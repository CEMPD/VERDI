Documentation:

	- User's manual can be found in the $VERDI_INSTALL_HOME/plugins/bootstrap/help 
	    folder or by clicking the 'Help' menu, then 'VERDI Help Docs' item after
	    one opens the VERDI GUI window.

3D Rendering:

	- Windows
		
		By default VERDI uses directX for 3D rendering. To use OpenGL
		comment out the the j3d line in the verdi.ini file.
		
Memory:

	- Windows
		To increase or decrease the amount of memory available to VERDI,
		change the value of the heapSize property in the verdi.ini file.
		
	- Linux / Unix / Mac
		Edit verdi.sh and replace the 512 in -Xmx512M with
		the new value. For example, -Xmx1024M.
		
Known Issues:

	- The color map for the contour display cannot use a custom interval
	- Animation that captures the display to a gif or a quicktime movie can
	  be slow.
	
