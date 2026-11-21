(()=>{
const Recognition=window.SpeechRecognition||window.webkitSpeechRecognition;
const mic='<svg aria-hidden="true" viewBox="0 0 24 24"><rect x="9" y="2" width="6" height="13" rx="3"/><path d="M5 10v2a7 7 0 0 0 14 0v-2M12 19v3M8 22h8"/></svg>';
const panel=document.createElement('dialog');panel.id='voiceDialog';panel.innerHTML='<h2>Set an alarm by voice</h2><p class="voice-help">Try “Set an alarm for seven thirty AM” or “Wake me in 20 minutes”.</p><button type="button" class="tonal voice-listen">'+mic+'<span>Start listening</span></button><p class="voice-feedback" role="status"></p><label class="fieldlabel" for="voiceCommand">Heard command, or type one</label><input id="voiceCommand" type="text" placeholder="Set an alarm for 7:30 AM" autocomplete="off"><p class="voice-help">Microphone starts only when you tap. Your browser may send audio to its speech service.</p><div class="actions"><button type="button" class="textbtn voice-cancel">Cancel</button><button type="button" class="tonal voice-review">Review alarm</button></div>';document.body.append(panel);
const feedback=panel.querySelector('.voice-feedback'),input=panel.querySelector('input'),listen=panel.querySelector('.voice-listen');let recognition=null,timer;
function stop(){clearTimeout(timer);if(recognition){const r=recognition;recognition=null;r.abort()}listen.querySelector('span').textContent='Start listening'}
function open(){feedback.textContent=Recognition?'Tap Start listening, then speak.':'Voice recognition is unavailable here. Open in Chrome or type your command below.';panel.showModal();input.value=''}
const bedside=document.createElement('button');bedside.className='tonal voice-trigger';bedside.innerHTML=mic+'Set alarm by voice';bedside.onclick=open;document.querySelector('.bedside-actions').append(bedside);
const top=document.createElement('button');top.className='iconbtn';top.setAttribute('aria-label','Set alarm by voice');top.title='Set alarm by voice';top.innerHTML=mic;top.onclick=open;document.querySelector('header').insertBefore(top,document.querySelector('#format'));
listen.disabled=!Recognition;listen.onclick=()=>{if(recognition){stop();feedback.textContent='Listening stopped. Review or edit the command below.';return}try{const r=new Recognition();recognition=r;r.lang='en-US';r.continuous=false;r.interimResults=false;r.onstart=()=>{listen.querySelector('span').textContent='Stop listening';feedback.textContent='Listening… Say an alarm time.'};r.onresult=e=>{input.value=e.results[0][0].transcript;receive(input.value)};r.onerror=e=>{feedback.textContent=({'not-allowed':'Microphone access was denied. Allow access in browser settings or type your command.','audio-capture':'No microphone found. Connect one or type your command.','no-speech':'No speech detected. Try again or type your command.','network':'Speech service could not connect. Try again or type your command.','service-not-allowed':'Speech recognition is unavailable in this browser. Type your command below.'})[e.error]||'Speech recognition stopped. Try again or type your command.'};r.onend=()=>{if(recognition===r){recognition=null;clearTimeout(timer);listen.querySelector('span').textContent='Start listening'}};r.start();timer=setTimeout(()=>{stop();feedback.textContent='Listening timed out. Try again or type your command.'},20000)}catch{stop();feedback.textContent='Could not start the microphone. Type your command instead.'}};
const numbers={zero:0,oh:0,one:1,two:2,three:3,four:4,five:5,six:6,seven:7,eight:8,nine:9,ten:10,eleven:11,twelve:12,thirteen:13,fourteen:14,fifteen:15,sixteen:16,seventeen:17,eighteen:18,nineteen:19,twenty:20,thirty:30,forty:40,fifty:50};
function normalize(s){return s.toLowerCase().replace(/a\.?\s*m\.?/g,'am').replace(/p\.?\s*m\.?/g,'pm').replace(/-/g,' ').replace(/o[’']?\s+clock/g,"o'clock").replace(/\b(twenty|thirty|forty|fifty)\s+(one|two|three|four|five|six|seven|eight|nine)\b/g,(_,a,b)=>String(numbers[a]+numbers[b])).replace(/\b(?:zero|oh|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty)\b/g,w=>String(numbers[w]))}
function parse(s){s=normalize(s.trim());if(!s)throw Error('Say or type an alarm time first.');let relative=s.match(/\bin\s+(\d+)\s*(minutes?|mins?|hours?|hrs?)\b/);let days=[];if(/\bweekdays\b/.test(s))days=[1,2,3,4,5];else if(/\bweekends\b/.test(s))days=[0,6];else if(/\b(every day|daily)\b/.test(s))days=[0,1,2,3,4,5,6];if(relative){if(days.length)throw Error('Use a clock time for repeating alarms, such as 7 AM every day.');let amount=+relative[1],mins=amount*(relative[2].startsWith('h')?60:1);if(mins<1||mins>1440)throw Error('Choose a delay from 1 minute to 24 hours.');let d=new Date(Date.now()+mins*60000);return{time:String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0'),days}}
if(/\b(tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday|next|tonight)\b/.test(s))throw Error('For now, use a time, “weekdays”, “weekends”, or “every day”. Choose a future date later in the Android app.');
let h,m=0,period;let match=s.match(/\b(\d{1,2})(?:\s*:\s*|\s+)(\d{1,2})(?:\s*(am|pm))?\b/);if(match){h=+match[1];m=+match[2];period=match[3]}else{match=s.match(/\b(\d{1,2})(?:\s*o'?clock)?\s*(am|pm)?\b/);if(match){h=+match[1];period=match[2]}}
if(h===undefined||m>59||h>23)throw Error('Use a time such as “7:30 AM” or “19:30”.');if(period&&(h<1||h>12))throw Error('Use hours 1 to 12 with AM or PM.');if(!period&&h>=1&&h<=12)throw Error('Please include AM or PM, for example “7:30 AM”.');if(period)h=h%12+(period==='pm'?12:0);return{time:String(h).padStart(2,'0')+':'+String(m).padStart(2,'0'),days}}
panel.querySelector('.voice-review').onclick=()=>{try{const result=parse(input.value);stop();panel.close();openEditor();document.querySelector('#alarmTime').value=result.time;selected=result.days;drawDays();document.querySelector('#alarmLabel').value='Voice alarm';document.querySelector('#editorTitle').textContent='Review voice alarm'}catch(e){feedback.textContent=e.message}};
panel.querySelector('.voice-cancel').onclick=()=>panel.close();panel.addEventListener('close',stop);panel.addEventListener('cancel',stop);document.addEventListener('visibilitychange',()=>{if(document.visibilityState==='hidden')stop()});
const hands=document.createElement('div');hands.className='handsfree';hands.innerHTML='<p class="handsfree-state" role="status"></p><button class="textbtn handsfree-toggle" type="button">Pause voice listening</button>';clockTarget();
function clockTarget(){document.querySelector('#clockView').append(hands)}
const state=hands.querySelector('p'),toggle=hands.querySelector('button');let enabled=true,background=null,retry=null,failures=0,pending=false,expiry=null;
function message(t){state.textContent=t}
function discard(){pending=false;clearTimeout(expiry)}
function shutdown(){clearTimeout(retry);if(background){const r=background;background=null;r.abort()}}
function halt(t){enabled=false;shutdown();toggle.textContent='Enable hands-free voice';message(t)}
let clarification=null;
function respond(kind,text){window.daybreakFeedback?.(kind,text);if(text){message(text);notify(text)}}
function saveSpoken(result){
 const previous=alarms;
 const same=alarms.find(a=>a.time===result.time&&JSON.stringify([...a.days].sort())===JSON.stringify([...result.days].sort()));
 const alarm={id:same?same.id:Math.max(Date.now(),...alarms.map(a=>Number(a.id)||0))+1,time:result.time,label:same?.label||'Voice alarm',days:result.days,enabled:true};
 alarms=same?alarms.map(a=>a.id===same.id?alarm:a):[...alarms,alarm];
 try{if(persist()===false)throw Error('Could not save the alarm. Please try again.')}catch(e){alarms=previous;localStorage.setItem('daybreak',JSON.stringify({alarms,use24,seconds}));render();respond('error',e.message);return}
 if(clarification&&document.querySelector('#editor').open)document.querySelector('#editor').close();clarification=null;render();
 const t=display(result.time),when=t.time+(t.period?' '+t.period:'');let summary='Alarm set for '+when+(result.days.length===7?', every day':result.days.length?', '+repeat(alarm):'')+'.';
 if(window.Android?.canScheduleAlarms&&!Android.canScheduleAlarms())summary='Alarm saved for '+when+'. Allow Alarms and reminders access before it can ring.';
 respond('saved',summary);
}
function receive(text){
 let raw=text.toLowerCase().trim().replace(/[.!?]+$/,'');const editor=document.querySelector('#editor');
 if(/^(?:start scan|start wake up scan|start wake-up scan)$/.test(raw)){respond('heard');if(window.alarmControl?.ringing){window.alarmControl.startScan();message('Wake-up scan requested.')}else respond('error','No alarm is ringing.');return}
 if(window.alarmControl?.ringing)return;
 if(clarification&&editor.open&&/^(?:a\.?\s*m\.?|p\.?\s*m\.?)$/.test(raw)){raw=clarification+' '+normalize(raw);}
 if(!/\bset\s+(?:an\s+)?alarm\b/.test(raw))return;
 if(editor.open&&!clarification)return;respond('heard');
 const command=raw.replace(/^.*?\bset\s+(?:an\s+)?alarm\b[, ]*/,'');
 if(!command){respond('error','Say set alarm for a time, such as six AM every day.');return}
 try{const result=parse(command);if(panel.open){stop();panel.close()}saveSpoken(result)}catch(e){
  clarification=raw;if(panel.open){stop();panel.close()}if(!editor.open)openEditor();
  document.querySelector('#alarmTime').value='';document.querySelector('#editorTitle').textContent='Clarify alarm time';
  let hint=editor.querySelector('.voice-clarification');if(!hint){hint=document.createElement('p');hint.className='voice-help voice-clarification';document.querySelector('#alarmTime').before(hint)}hint.textContent=e.message;
  respond('error',e.message);
 }
}
function begin(){if(!window.daybreakSetupReady||!enabled||!Recognition||background||document.visibilityState!=='visible'||panel.open)return;try{let r=new Recognition();background=r;r.lang='en-US';r.continuous=true;r.interimResults=false;r.onstart=()=>{failures=0;message('Listening for “Set alarm”. Microphone is on.');toggle.textContent='Pause voice listening'};r.onresult=e=>{for(let i=e.resultIndex||0;i<e.results.length;i++){if(e.results[i].isFinal!==false)receive(e.results[i][0].transcript)}};r.onerror=e=>{if(['not-allowed','service-not-allowed','audio-capture'].includes(e.error)){halt('Voice listening unavailable. Allow microphone access in your browser, then enable hands-free voice.')}else if(e.error!=='aborted'&&e.error!=='no-speech'){failures++;if(failures>=3)halt('Speech service is unavailable. Check your connection, then enable hands-free voice.')}};r.onend=()=>{if(background!==r)return;background=null;if(enabled&&document.visibilityState==='visible')retry=setTimeout(begin,1200)};r.start()}catch{halt('Your browser requires microphone permission or a tap on Enable hands-free voice before listening.')}}
toggle.onclick=()=>{if(enabled){halt('Voice listening paused.');discard()}else{window.daybreakSetupReady=true;enabled=true;failures=0;begin()}};
panel.addEventListener('close',()=>{if(enabled)retry=setTimeout(begin,500)});const oldOpen=top.onclick;top.onclick=()=>{shutdown();oldOpen()};bedside.onclick=top.onclick;
document.querySelector('#editor').addEventListener('close',()=>{discard();clarification=null;document.querySelector('.voice-clarification')?.remove()});document.addEventListener('visibilitychange',()=>{if(document.visibilityState==='hidden'){shutdown();message('Voice listening paused while this page is hidden.')}else if(enabled)begin()});
document.addEventListener('daybreak-speaking',shutdown);document.addEventListener('daybreak-speaking-ended',()=>{if(enabled)retry=setTimeout(begin,400)});document.addEventListener('daybreak-setup-ready',()=>{if(enabled)begin()});if(Recognition){message('Complete permission setup to enable hands-free voice.');if(window.daybreakSetupReady)begin()}else{enabled=false;toggle.disabled=true;message('Hands-free voice is unavailable in this browser. Try Chrome on your tablet.')}
})();






