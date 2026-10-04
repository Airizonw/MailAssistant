"""Check preservation and source crops, and render asset contact sheets for review."""
import hashlib
from build import validate
import io
import json
from pathlib import Path
import sqlite3
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
db = sqlite3.connect(ROOT / 'src/main/resources/database/exercise.db')
db.execute('PRAGMA foreign_keys = ON')
validate(db)
backups = list((HERE / 'backups').glob('*.db'))
original = None
for candidate in backups:
    with sqlite3.connect(candidate) as previous:
        if previous.execute('SELECT count(*) FROM exercise').fetchone()[0] == 45:
            original = candidate
            for table, condition in [('chapter', 'id<=4'), ('exercise', 'chapter_id<=4'),
                                     ('exercise_asset', 'exercise_id<5000')]:
                sql = f'SELECT * FROM {table} WHERE {condition} ORDER BY 1,2'
                assert previous.execute(sql).fetchall() == db.execute(sql).fetchall(), table
            for row in previous.execute('SELECT * FROM asset'):
                assert row == db.execute('SELECT * FROM asset WHERE id=?', (row[0],)).fetchone()
            break
if original is None:
    print('SKIP: historical 45-question backup absent; preservation comparison not performed.')
meta = dict(db.execute('SELECT key,value FROM metadata'))
asset_sources = json.loads(meta['asset_sources'])
review = HERE / 'review'
review.mkdir(exist_ok=True)
assets = []
for aid, source in asset_sources.items():
    data = db.execute('SELECT data FROM asset WHERE id=?', (aid,)).fetchone()[0]
    with Image.open(HERE / 'sources' / source['file']) as scan:
        crop = scan.crop(source['box']).convert('RGB')
    with Image.open(io.BytesIO(data)) as stored:
        assert crop.size == stored.size and crop.tobytes() == stored.convert('RGB').tobytes()
    if int(source['file'][7:9]) >= 5:
        ids = ','.join(str(r[0]) for r in db.execute('SELECT exercise_id FROM exercise_asset WHERE asset_id=?', (aid,)))
        assets.append((ids, source, crop))
for page in range((len(assets) + 7) // 8):
    sheet = Image.new('RGB', (1240, 1440), '#dddddd')
    draw = ImageDraw.Draw(sheet)
    for cell, (ids, source, crop) in enumerate(assets[page*8:page*8+8]):
        x, y = (cell % 2)*620, (cell // 2)*360
        draw.text((x+12, y+8), f"{ids} | {source['file']} | {source['box']}", fill='black')
        crop.thumbnail((600, 325))
        sheet.paste(crop, (x+10, y+30))
    sheet.save(review / f'assets-{page+1}.png')
if original is not None:
    print('Preserved all 45 original exercises, 4 chapters, and 13 assets byte-for-byte.')
print(f'Checked {len(asset_sources)} exact source crops; {len(assets)} new assets rendered for review.')
print('Chapter counts:', db.execute('SELECT chapter_id,count(*) FROM exercise GROUP BY chapter_id').fetchall())
print('Database SHA-256:', hashlib.sha256((ROOT / 'src/main/resources/database/exercise.db').read_bytes()).hexdigest())
print('Original backup:', original)
db.close()
