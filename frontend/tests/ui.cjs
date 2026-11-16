const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'C:/Users/laksh/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const assert = require('node:assert/strict');
const path=require('node:path');
const server=require('node:child_process').spawn(process.execPath,['node_modules/vite/bin/vite.js','--host','127.0.0.1','--port','5175'],{stdio:'pipe',windowsHide:true});
process.on('exit',()=>server.kill());
(async()=>{
 await new Promise((resolve,reject)=>{server.stdout.on('data',d=>{if(d.toString().includes('Local:'))resolve();});server.on('error',reject);setTimeout(()=>reject(Error('Server timeout')),15000).unref();});
 const browser=await chromium.launch({headless:true,executablePath:'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'});
 const page=await browser.newPage({viewport:{width:390,height:844},deviceScaleFactor:1});const errors=[];page.on('pageerror',e=>errors.push(e.message));
 await page.addInitScript(()=>localStorage.setItem('at.onboard','1'));
 await page.goto('http://127.0.0.1:5175');await page.getByRole('heading',{name:'Make room for you.'}).waitFor();
 await page.getByPlaceholder('e.g. Revise chapter 3').fill('Revise chapter 3');
 await page.getByLabel('Focus minutes',{exact:true}).selectOption('1');await page.getByLabel('Break minutes',{exact:true}).selectOption('1');
 await page.getByRole('button',{name:'Start study session',exact:true}).click();await page.getByRole('button',{name:'Pause session',exact:true}).waitFor();
 await page.getByRole('button',{name:'Pause session',exact:true}).click();await page.getByRole('button',{name:'Resume session',exact:true}).waitFor();
 await page.reload();await page.getByRole('button',{name:'Resume session',exact:true}).waitFor();
 await page.clock.install();await page.getByRole('button',{name:'Resume session',exact:true}).click();await page.getByRole('button',{name:'Pause session',exact:true}).waitFor();
 await page.clock.fastForward(61000);await page.getByRole('button',{name:'Take 1 minute break',exact:true}).waitFor();
 await page.getByRole('button',{name:'Take 1 minute break',exact:true}).click();await page.getByText('Stand, stretch, or look away from your screen.').waitFor();
 await page.clock.fastForward(61000);await page.getByRole('button',{name:'Finish session',exact:true}).click();
 await page.getByRole('tab',{name:'Reflect',exact:true}).click();await page.getByLabel('What helped today? (optional)',{exact:true}).fill('Putting the phone away helped.');
 await page.getByRole('button',{name:'Save today’s reflection',exact:true}).click();await page.getByText('Putting the phone away helped.',{exact:true}).waitFor();
 const stored=await page.evaluate(()=>JSON.parse(localStorage.getItem('attentiontwin.wellness.v1')));assert.equal(stored.completed.length,1);assert.equal(stored.completed[0].minutes,1);
 await page.screenshot({path:path.resolve('test-results/reflection.png'),fullPage:true});
 await page.getByRole('tab',{name:'Wind down',exact:true}).click();await page.getByLabel('Target bedtime',{exact:true}).fill('23:00');await page.getByRole('button',{name:'Set next bedtime reminder',exact:true}).click();await page.getByRole('button',{name:'Cancel reminder',exact:true}).waitFor();
 await page.getByText('Put the phone out of reach',{exact:true}).click();assert.equal(await page.getByLabel('Put the phone out of reach',{exact:true}).isChecked(),true);
 await page.screenshot({path:path.resolve('test-results/bedtime.png'),fullPage:true});
 await page.getByRole('tab',{name:'Check-in',exact:true}).click();await page.getByLabel('Remind me in',{exact:true}).selectOption('1');await page.getByRole('button',{name:'Start mindful reminder',exact:true}).click();await page.getByRole('button',{name:'I’ve checked in',exact:true}).waitFor();await page.clock.fastForward(61000);await page.getByText('Time for a breath. Is scrolling still what you want to do?',{exact:true}).waitFor();await page.getByRole('button',{name:'I’ve checked in',exact:true}).click();
 await page.getByRole('tab',{name:'Study',exact:true}).click();await page.screenshot({path:path.resolve('test-results/study.png'),fullPage:true});
 assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);
 const navY=await page.locator('.bottom-nav button').evaluateAll(items=>items.map(x=>x.getBoundingClientRect().top));assert.equal(new Set(navY).size,1);
 await page.setViewportSize({width:320,height:740});assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);await page.setViewportSize({width:390,height:844});
 await page.getByRole('tab',{name:'Reflect',exact:true}).click();page.once('dialog',d=>d.accept());await page.getByRole('button',{name:'Erase wellness data',exact:true}).click();await page.getByText('Wellness data erased.',{exact:true}).waitFor();
 const erased=await page.evaluate(()=>JSON.parse(localStorage.getItem('attentiontwin.wellness.v1')));assert.equal(erased.completed.length,0);assert.equal(erased.reflections.length,0);assert.equal(erased.scrollEnd,0);
 assert.deepEqual(errors,[]);console.log('PASS: study/pause/reload/resume/completion/break, reflection, bedtime checklist, mindful reminder, deletion, mobile overflow, no runtime errors');await browser.close();server.kill();
})().catch(e=>{console.error(e);process.exit(1)});


