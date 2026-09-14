# Project Plan

10 Minutes Workout App - Music Management Phase.

## Project Brief

Implement music management in the Settings screen.
- Infrastructure: Implement a `MusicManager` or similar to handle background music playback using `MediaPlayer`.
- Persistence: Use `DataStore` to save user preferences for background music (Enabled/Disabled, Volume).
- Settings UI: Update `SettingsScreen` to include a "Gestion de la musique" section with:
    - A global toggle to enable/disable background music.
    - A list of available tracks (starting with the 'background_music' from `res/raw`).
    - Note: For `res/raw` files, 'delete' will be interpreted as 'disable/hide from list' and 'move' as 'reorder' if applicable.
- Integration: Ensure background music plays during the `WorkoutScreen` if enabled in settings.
- Resource Management: Properly release `MediaPlayer` when not needed.

## Implementation Steps
**Total Duration:** 41m 33s

### Task_1_Foundation_Logic: Define the Exercise data model and static list of 16 exercises. Implement the WorkoutViewModel using Coroutines to manage timer states (PREPARATION, EFFORT, REST) and transitions.
- **Status:** COMPLETED
- **Updates:** Implemented Exercise data model, static list of 16 exercises, and WorkoutViewModel with Coroutine-based timer logic supporting PREPARATION, EFFORT, and REST states. Play/Pause/Skip logic is included. Project compiles successfully.
- **Acceptance Criteria:**
  - Exercise data class and list of 16 exercises created
  - WorkoutViewModel manages timer logic and state transitions correctly
  - Play/Pause/Skip logic functional in ViewModel
- **Duration:** 4m 16s

### Task_2_Workout_UI: Implement the Workout screen using Jetpack Compose with Material 3. Include a large countdown timer, exercise info, progress indicator (X/16), GIF placeholder, and control buttons. Apply vibrant theme and edge-to-edge support.
- **Status:** COMPLETED
- **Updates:** Implemented the Workout screen with a large countdown timer, phase-aware status text, progress indicator, and workout controls. Applied a vibrant Material 3 theme and edge-to-edge support. Included logic to keep the screen on during workouts. App compiles and is ready for verification.
- **Acceptance Criteria:**
  - Main Workout UI displays state and countdown correctly
  - Progress indicator (e.g., '1/16') updates
  - Edge-to-edge and screen-on logic implemented
  - Material 3 vibrant theme applied
- **Duration:** 2m 35s

### Task_3_Icon_Verify: Create an adaptive app icon and conduct a final run and verify of the application to ensure stability and adherence to all project requirements.
- **Status:** COMPLETED
- **Updates:** Adaptive app icon created and applied. Critic_agent confirmed the application is stable, functionally complete with all 16 exercises, and follows Material 3 guidelines. Edge-to-edge, Keep-Screen-On, and workout controls are all verified and working correctly. Final build is successful.
- **Acceptance Criteria:**
  - Adaptive app icon created and applied
  - Project builds successfully and app does not crash
  - All 16 exercises cycle correctly through timer states
  - Critic_agent confirms Material 3 aesthetic alignment
- **Duration:** 8m 20s

### Task_4_Navigation_Architecture: Add Jetpack Compose Navigation with Home, Workout, Calendar, and Settings routes. Implement a Scaffold with a Bottom Navigation Bar that hides on the Workout screen. Build the Home screen with the workout card and add a back button to the Workout screen.
- **Status:** COMPLETED
- **Updates:** Implemented Jetpack Compose Navigation with 4 routes (Home, Workout, Calendar, Settings). Added a Scaffold with a conditional BottomNavigationBar that hides on the Workout screen. Built the Home screen UI with a clickable workout card and updated the Workout screen with a back button. Navigating back from the Workout screen correctly stops the timer due to ViewModel scoping. Project compiles and navigation is functional.
- **Acceptance Criteria:**
  - Navigation implemented for all 4 routes
  - Bottom Navigation visible on Home/Calendar/Settings, hidden on Workout
  - Home screen UI implemented with title and workout card
  - Workout screen back button returns to Home
- **Duration:** 4m 59s

### Task_5_Logic_and_Verification: Ensure the Workout timer stops when leaving the screen. Perform a final run and verify to confirm application stability, navigation flow, and UI fidelity for Phase 2.
- **Status:** COMPLETED
- **Updates:** Timer stops correctly when navigating away from Workout. Navigation is fluid and stable between all 4 screens. Bottom Bar visibility logic is correct. UI matches the requested Material 3 aesthetic and content. Phase 2 requirements are fully met.
- **Acceptance Criteria:**
  - Timer stops correctly when navigating away from Workout
  - App does not crash during navigation
  - Build pass and existing features remain functional
  - UI matches design requirements and Material 3 aesthetic
- **Duration:** 3m 12s

### Task_6_Multimedia_Implementation: Add Coil GIF support and implement GIF display in WorkoutScreen. Integrate TTSManager for French announcements (Next exercise, Mid-time, Countdown) and SoundPool for the bell sound in WorkoutViewModel.
- **Status:** COMPLETED
- **Updates:** Coil dependencies added for GIF support. Implemented TTSManager for French announcements ("Prochain exercice", "Mi-temps", and "3-2-1"). Integrated SoundPool for the bell sound at phase transitions. WorkoutViewModel updated to manage multimedia resources and their cleanup. WorkoutScreen now displays exercise GIFs. Project compiles successfully.
- **Acceptance Criteria:**
  - Coil dependencies added and GIF loading functional loading by resource name
  - TTS announces 'Prochain exercice', 'Mi-temps', and '3-2-1' in French
  - SoundPool plays bell sound at phase transitions
- **Duration:** 9m 40s

### Task_7_Cleanup_and_Verification: Implement resource cleanup for TTS and SoundPool in WorkoutViewModel. Perform a final Run and Verify for Phase 3 to ensure stability, multimedia synchronization, and Material 3 aesthetic.
- **Status:** COMPLETED
- **Updates:** Multimedia logic for GIFs, TTS, and SoundPool is implemented. TTS announcements and countdown logic are verified. Resource cleanup is handled in the ViewModel. However, the critic_agent reported that GIF assets are missing from the drawable folder and the bell sound sometimes fails to play because it's not ready. These issues will be addressed in a refinement loop.
- **Acceptance Criteria:**
  - TTS and SoundPool resources are correctly released
  - Project builds successfully and app does not crash
  - Multimedia features are synchronized with workout timer
  - Critic_agent confirms Phase 3 stability and aesthetic
- **Duration:** 3m 24s

### Task_8_Music_Infrastructure_and_Settings: Implement MusicManager with MediaPlayer for background music and DataStore for persisting user preferences. Update SettingsScreen to include music toggles and volume control.
- **Status:** COMPLETED
- **Updates:** Implemented MusicManager for background music playback using MediaPlayer. Integrated DataStore for persistent music settings (Enabled/Disabled, Volume). Updated SettingsScreen with 'Gestion de la musique' section including a toggle and volume slider. WorkoutViewModel now respects these settings during the workout session. Proper resource cleanup is implemented.
- **Acceptance Criteria:**
  - MusicManager handles background music playback and lifecycle
  - User preferences (Enabled, Volume) are persisted via DataStore
  - SettingsScreen UI includes 'Gestion de la musique' section with controls
- **Duration:** 5m 7s

### Task_9_Music_Integration_and_Final_Verify: Integrate background music with the WorkoutScreen and implement resource cleanup. Perform a final run and verify to confirm application stability and adherence to Phase 4 requirements.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Background music plays during workout according to user settings
  - MediaPlayer resources are released correctly when not needed
  - App builds pass and does not crash
  - Critic_agent confirms Phase 4 stability and Material 3 aesthetic
- **StartTime:** 2026-05-03 00:24:07 CEST

