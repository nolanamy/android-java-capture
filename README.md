android-java-capture
====================

Trying to get consistent capture without (necessarily) drawing it to the screen.

**Camera opener:**

- Camera.open()
  - open the default camera
  - assumes there's only one
- Camera.open(int)
  - open the camera at the specified index

**Preview output:**

- setPreviewDisplay (SurfaceHolder)
  - requires an actual SurfaceView in the view hierarchy
  - will display images on that view; must obscure/overlay to hide
- setPreviewTexture (SurfaceTexture)
  - accepts a dummy SurfaceTexture; no actual View needed

**Version Failover:**

 - if HONEYCOMB <= VERSION
   - Camera.open(int)
   - Preview sent to SurfaceTexture
 - if GINGERBREAD <= VERSION < HONEYCOMB
   - Camera.open(int)
   - Preview sent to SurfaceView
 - if VERSION < GINGERBREAD
   - Camera.open()
   - Preview sent to SurfaceView

TODO:

 - ? Add native capture option
