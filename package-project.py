"""Package source and the reviewed submission outputs without local build caches."""
from pathlib import Path
import subprocess
import zipfile

root = Path(__file__).resolve().parent
target = root.parent / 'AttentionTwin-Project.zip'
names = subprocess.check_output(['git', 'ls-files', '-co', '--exclude-standard', '-z'], cwd=root).decode('utf-8').split('\0')
with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
    for name in sorted(set(names)):
        if not name:
            continue
        source = root / name
        if source.is_file():
            archive.write(source, f'AttentionTwin/{name}')
with zipfile.ZipFile(target) as archive:
    assert archive.testzip() is None
    assert 'AttentionTwin/deliverables/AttentionTwin-Wellness.apk' in archive.namelist()
    assert 'AttentionTwin/deliverables/AttentionTwin-Presentation.pptx' in archive.namelist()
print(f'Project ZIP verified: {target} ({target.stat().st_size} bytes)')
