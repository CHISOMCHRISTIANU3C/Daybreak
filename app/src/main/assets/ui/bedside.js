(()=>{
const clock=document.querySelector('#clockView');
const controls=document.createElement('div');controls.className='bedside-actions';
const dim=document.querySelector('#dim');dim.onclick=null;controls.append(dim);
const start=document.createElement('button');start.className='tonal bedside-start';start.textContent='Start bedside mode';controls.prepend(start);
const status=document.createElement('p');status.className='bedside-status';status.setAttribute('role','status');
clock.append(controls,status);
const exit=document.createElement('button');exit.type='button';exit.className='bedside-exit tonal';exit.textContent='Return to app';exit.hidden=true;clock.append(exit);let exitTimer;
function hideExit(){clearTimeout(exitTimer);exit.hidden=true}
function revealExit(event){if(!active||event.target.closest('button,dialog,input'))return;exit.hidden=false;clearTimeout(exitTimer);exitTimer=setTimeout(hideExit,6000)}
clock.addEventListener('click',revealExit);exit.onclick=()=>stop();
const clockDigits=clock.querySelector('.digits');clockDigits.setAttribute('tabindex','0');clockDigits.setAttribute('role','button');clockDigits.setAttribute('aria-label','Clock. Activate to reveal Return to app.');clockDigits.addEventListener('keydown',e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();revealExit({target:clockDigits})}});
let active=false,wake=null,dimmed=false,starting=false;
function updateStatus(message){status.textContent=message|| (active?(wake&&!wake.released?'Screen kept awake. ':'Screen awake lock unavailable. '):'Start bedside mode to keep this display awake. ')+'Allow Do Not Disturb in Settings to reduce interruptions.';}
async function acquire(){if(!active||document.visibilityState!=='visible')return;try{if(!navigator.wakeLock)throw new Error('unsupported');const lock=await navigator.wakeLock.request('screen');if(!active){await lock.release();return}wake=lock;lock.addEventListener('release',()=>{if(wake===lock){wake=null;updateStatus()}})}catch{wake=null}updateStatus()}
async function stop(){active=false;hideExit();document.body.classList.remove('focus-mode');start.textContent='Start bedside mode';start.disabled=false;if(wake){const lock=wake;wake=null;await lock.release().catch(()=>{})}if(document.fullscreenElement)await document.exitFullscreen().catch(()=>{});updateStatus()}
start.onclick=async()=>{if(starting)return;if(active){await stop();return}starting=true;active=true;start.disabled=true;document.body.classList.add('focus-mode');start.textContent='Exit bedside mode';notify('Tap the clock to reveal Return to app.');let full=Promise.resolve();try{if(document.documentElement.requestFullscreen)full=document.documentElement.requestFullscreen().catch(()=>{})}catch{}await acquire();await full;starting=false;start.disabled=false};
dim.onclick=()=>{dimmed=!dimmed;clock.querySelectorAll(':scope > .date,:scope > .digits,:scope > p:not(.bedside-status)').forEach(e=>e.style.opacity=dimmed?'.35':'1');dim.textContent=dimmed?'Brighten display':'Dim display'};
document.addEventListener('visibilitychange',()=>{if(document.visibilityState==='visible'&&active)acquire()});document.addEventListener('fullscreenchange',()=>{if(!document.fullscreenElement&&active&&!starting)stop()});document.addEventListener('keydown',e=>{if(e.key==='Escape'&&active)stop()});
document.querySelectorAll('[data-device]').forEach(b=>b.addEventListener('click',()=>{if(active)stop();if(b.dataset.device==='tablet')document.querySelector('[data-view="clock"]').click()}));
if(document.body.classList.contains('tablet'))document.querySelector('[data-view="clock"]').click();updateStatus();
})();

