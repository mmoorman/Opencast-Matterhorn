var CAPTURE_AGENT_CONFIDENCE_MONITORING_URL = "/confidence/rest";
var Monitor = {} || Monitor;
var AudioBar = {} || AudioBar;
var Monitor.intervalImgId = null;
var Monitor.intervalAudioId = null;
var Monitor.selectedDevice = null;

Monitor.loadDevices = function(){
  //load the devices
  $.get(CAPTURE_AGENT_CONFIDENCE_MONITORING_URL + "/devices", function(data){
      //do stuff to make a device array.
      var devices = $('name', data);
      for(d in devices){
        devices[d].text();
        $('#device_tab').append('<li>' + devices[d] + '</li>');
      }
      });
}

Monitor.selectDevice = function(device){
  if(Monitor.selectedDevice != device){
    if(Monitor.intervalId){ clearInterval(Monitor.intervalImgId); clearInterval(Monitor.intervalAudioId); }
    Monitor.intervalImgId = setInterval(Monitor.updateImg, 5000);
    Monitor.intervalAudioId = setInterval(Monitor.updateAudio, 1000);
  }
}

Monitor.updateImg = function(){
  var imgGrab = CAPTURE_AGENT_CONFIDENCE_MONITORING_URL + "/" + Monitor.selectedDevice;
  $("#image_preview").replaceWith('<img src="' + imgGrab + '"></img>');
}

Monitor.updateAudio = function(){
  
}

AudioBar.setValue = function(){
  
}