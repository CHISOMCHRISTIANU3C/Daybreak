(()=>{
function condition(code,day=true){if(code===0)return{kind:'clear',label:day?'Clear sky':'Clear night'};if([1,2,3].includes(code))return{kind:'cloud',label:code===3?'Overcast':'Partly cloudy'};if([45,48].includes(code))return{kind:'fog',label:'Fog'};if([71,73,75,77,85,86].includes(code))return{kind:'snow',label:'Snow'};if([51,53,55,56,57,61,63,65,66,67,80,81,82].includes(code))return{kind:'rain',label:'Rain'};if([95,96,99].includes(code))return{kind:'storm',label:'Thunderstorm'};return{kind:'unknown',label:'Weather unavailable'};}
function indoor(sensor,manual,now=Date.now()){
 if(sensor?.supported&&sensor.active&&sensor.reliable&&Number.isFinite(sensor.celsius)&&sensor.celsius>=-50&&sensor.celsius<=85&&Number.isFinite(sensor.ageMs)&&sensor.ageMs>=0&&(sensor.onChange===true||sensor.ageMs<=300000))return{value:sensor.celsius,source:'Device ambient sensor',fresh:true};
 if(manual&&Number.isFinite(manual.value)&&Number.isFinite(manual.time)&&now-manual.time>=0&&now-manual.time<=3600000)return{value:manual.value,source:'Manual thermometer reading',fresh:true};
 let reason=manual?'Manual reading out of date':sensor==null?'Temperature access unavailable':sensor.supported===false?'No room-temperature sensor':!sensor.active?'Room sensor is paused':sensor.celsius==null?'Waiting for a room-sensor reading':!sensor.reliable?'Room-sensor reading is unreliable':'Room-sensor reading is out of date';
 return{value:null,source:reason,fresh:false};
}
function advice(room,target){if(!Number.isFinite(room.value))return 'Add a recent room thermometer reading to compare with your target.';const difference=room.value-target;if(Math.abs(difference)<=1)return 'Your room is close to your '+target+'°C target.';return difference>0?'Your room is above your target. Consider lowering the thermostat toward '+target+'°C.':'Your room is below your target. Consider raising the thermostat toward '+target+'°C.';}
window.DaybreakClimate={condition,indoor,advice};
})();

