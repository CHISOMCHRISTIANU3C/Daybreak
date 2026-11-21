# Third-party components

- MediaPipe Tasks Vision 0.10.21, Apache License 2.0. Source and documentation: https://github.com/google-ai-edge/mediapipe and https://developers.google.com/edge/mediapipe/solutions/vision/pose_landmarker . JavaScript and WebAssembly files are bundled under `app/src/main/assets/ui/vendor`.
- Google MediaPipe Pose Landmarker Lite, float16, model version 1. Downloaded from https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task . See the upstream model documentation and license before distribution.
- Gradle wrapper 8.13 is included to make the project reproducible. Android Gradle Plugin 8.12.0 and the Android SDK are build dependencies.
- The twin-bell audio is synthesized for this project. It is an approximation, not a recording of the reference photograph's clock.

- Weather data is provided by Open-Meteo, https://open-meteo.com/ . Attribution is included in Settings. Review https://open-meteo.com/en/terms for service and data-license terms before distribution.
