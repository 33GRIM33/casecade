from pathlib import Path
import subprocess,zipfile
root=Path(__file__).resolve().parent
target=root.parent/'AttentionTwin-5.0-Project.zip'
names=subprocess.check_output(['git','ls-files','-co','--exclude-standard','-z'],cwd=root).decode('utf8').split('\0')
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for name in sorted(set(names)):
  if not name or name.startswith(('deliverables/','release-v2/','release-v3/','release-v4/')):continue
  source=root/name
  if source.is_file():z.write(source,'AttentionTwin/'+name)
with zipfile.ZipFile(target) as z:
 assert z.testzip() is None
 assert 'AttentionTwin/release-v5/AttentionTwin-5.0.apk' in z.namelist()
print(f'Verified {target} ({target.stat().st_size} bytes)')
