/**
 * Application "Videodisplay.mxml"
 */

/**
 * The "Videodisplay" javascript namespace. All the functions/variables you
 * have selected under the "Videodisplay.mxml" in the tree will be
 * available as static members of this namespace object.
 */
Videodisplay = {};


/**
 * Listen for the instantiation of the Flex application over the bridge
 */
FABridge.addInitializationCallback("b_Videodisplay", VideodisplayReady);


/**
 * Hook here all the code that must run as soon as the "Videodisplay" class
 * finishes its instantiation over the bridge.
 *
 * However, using the "VideodisplayReady()" is the safest way, as it will 
 * let Ajax know that involved Flex classes are available for use.
 */
function VideodisplayReady() {

	// Initialize the "root" object. This represents the actual 
	// "Videodisplay.mxml" flex application.
	b_Videodisplay_root = FABridge["b_Videodisplay"].root().getFlexAjaxBridge();
	
	// Global functions in the "Videodisplay.mxml" application

	Videodisplay.play = function() {
		b_Videodisplay_root.play();
	};

	Videodisplay.stop = function() {
		b_Videodisplay_root.stop();
	};

	Videodisplay.pause = function() {
		b_Videodisplay_root.pause();
	};
	
	Videodisplay.skipBackward = function() {
		b_Videodisplay_root.skipBackward();
	};
	
	Videodisplay.rewind = function() {
		b_Videodisplay_root.rewind();
	};
	
	Videodisplay.fastForward = function() {
		b_Videodisplay_root.fastForward();
	};
	
	Videodisplay.skipForward = function() {
		b_Videodisplay_root.skipForward();
	};
	
	Videodisplay.passCharCode = function(argInt){
		b_Videodisplay_root.passCharCode(argInt);
	};
	
	Videodisplay.setVolume = function(argNumber) {
		b_Videodisplay_root.setVolume(argNumber);
	};

	Videodisplay.getVolume = function(){
		return b_Videodisplay_root.getVolume();
	};
	
	Videodisplay.seek = function(argNumber) {
		b_Videodisplay_root.seek(argNumber);
	};
	
	Videodisplay.setLanguage = function(argString) {
		b_Videodisplay_root.setLanguage(argString);
	};
	
	Videodisplay.closedCaptions = function(argBool) {
		b_Videodisplay_root.closedCaptions(argBool);
	};
	
	Videodisplay.setccBool = function(ccBool) {
		b_Videodisplay_root.setccBool(ccBool);
	};

	Videodisplay.setMediaURL = function(argString) {
		b_Videodisplay_root.setMediaURL(argString);
	};
	
	Videodisplay.setCaptionsURL = function(argString) {
		b_Videodisplay_root.setCaptionsURL(argString);
	};
	
	Videodisplay.setPlayerId = function(argString) {
		b_Videodisplay_root.setPlayerId(argString);
	};
}
