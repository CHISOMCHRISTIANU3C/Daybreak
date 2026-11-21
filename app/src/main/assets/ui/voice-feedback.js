(()=>{
let context;
function beep(){try{context??=new (window.AudioContext||window.webkitAudioContext)();context.resume().then(()=>{const now=context.currentTime,o=context.createOscillator(),g=context.createGain();o.frequency.value=880;g.gain.setValueAtTime(0,now);g.gain.linearRampToValueAtTime(.12,now+.01);g.gain.exponentialRampToValueAtTime(.001,now+.13);o.connect(g);g.connect(context.destination);o.start(now);o.stop(now+.15)})}catch{}}
window.daybreakFeedback=(kind,text='')=>{if(window.Android?.voiceFeedback){Android.voiceFeedback(kind,text);return}if(kind==='heard')beep();else if(text&&window.speechSynthesis){const u=new SpeechSynthesisUtterance(text);u.lang='en-US';speechSynthesis.cancel();document.dispatchEvent(new Event('daybreak-speaking'));let ended=false;const done=()=>{if(ended)return;ended=true;document.dispatchEvent(new Event('daybreak-speaking-ended'))};u.onend=done;u.onerror=done;setTimeout(done,12000);speechSynthesis.speak(u)}};
})();
