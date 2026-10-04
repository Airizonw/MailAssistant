from pathlib import Path
import hashlib
import sqlite3
import zipfile

root = Path(__file__).resolve().parents[1]
release = root / 'target/release/MailAssistant'
source = root / 'src/main/resources/database/exercise.db'
runtime = release / 'data/database/exercise.db'
expected = source.read_bytes()
assert runtime.read_bytes() == expected, 'Runtime bank was not upgraded'
with sqlite3.connect(runtime) as db:
    assert db.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
    assert not db.execute('PRAGMA foreign_key_check').fetchall()
    assert db.execute('SELECT count(*) FROM chapter').fetchone()[0] == 19
    assert db.execute('SELECT count(*) FROM exercise').fetchone()[0] == 173
    assert db.execute("SELECT value FROM metadata WHERE key='content_version'").fetchone()[0] == '2026.10.03.1'
jar = next((release / 'app').glob('mail-assistant-*.jar'))
with zipfile.ZipFile(jar) as archive:
    assert archive.read('database/exercise.db') == expected
    assert 'com/mailassistant/desktop/SingleInstanceGuard.class' in archive.namelist()
backup = root / 'tools/runtime-data-backups/20261003-update/MailAssistant/data'
for file in backup.rglob('*'):
    if not file.is_file():
        continue
    relative = file.relative_to(backup)
    if relative.as_posix() == 'database/exercise.db':
        digest = hashlib.sha256(file.read_bytes()).hexdigest()
        restored = release / 'data/database/backups' / f'exercise-{digest}.db'
    else:
        restored = release / 'data' / relative
    assert restored.read_bytes() == file.read_bytes(), f'Preservation mismatch: {relative}'
executables = list((root / 'target').rglob('MailAssistant.exe'))
assert executables == [release / 'MailAssistant.exe'], executables
print('PASS: packaged and runtime bank both contain 19 chapters / 173 exercises.')
print('PASS: old database backed up; existing draft and configuration bytes preserved.')
print('PASS: only the latest MailAssistant.exe remains under target/.')
