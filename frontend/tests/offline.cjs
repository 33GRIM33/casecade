const { chromium }=require('C:/Users/laksh/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const {pathToFileURL}=require('node:url');
const path=require('node:path');
const assert=require('node:assert/strict');
(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'});
 const page=await browser.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.context().setOffline(true);await page.goto(pathToFileURL(path.resolve('../deliverables/offline-demo.html')).href);
 await page.getByRole('button',{name:'Set up my phone',exact:true}).click();
 await page.getByRole('button',{name:'Continue to app settings',exact:true}).click();
 await page.getByRole('button',{name:'Skip for now',exact:true}).click();
 await page.getByRole('heading',{name:'Make room for you.',exact:true}).waitFor();
 await page.getByRole('tab',{name:'Reflect',exact:true}).click();
 await page.getByLabel('What helped today? (optional)',{exact:true}).fill('Offline demo works.');
 await page.getByRole('button',{name:'Save today’s reflection',exact:true}).click();await page.getByText('Offline demo works.',{exact:true}).waitFor();
 await page.reload();await page.getByRole('tab',{name:'Reflect',exact:true}).click();await page.getByText('Offline demo works.',{exact:true}).waitFor();
 assert.deepEqual(errors,[]);await browser.close();console.log('PASS: standalone file opens offline, onboarding works, reflection survives reload, no runtime errors');
})().catch(e=>{console.error(e);process.exit(1)});
