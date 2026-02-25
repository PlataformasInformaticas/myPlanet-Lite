# Dead Code Analysis Report - myplanet Android

This report identifies unused resources, code, and dependencies in the `myplanet` Android repository.

## 1. Kotlin/Java Dead Code

### Unused Classes/Files
The following files are not referenced anywhere in the codebase and are not registered in `AndroidManifest.xml`.
- `app/src/main/java/org/ole/planet/myplanet/lite/DashboardTeamMembersViewModel.kt`: Older ViewModel implementation for team members, replaced by direct logic in `DashboardTeamMembersFragment`.
- `app/src/main/java/org/ole/planet/myplanet/lite/DashboardTeamMembersAdapter.kt`: Older Adapter implementation for team members, replaced by an inner class in `DashboardTeamMembersFragment`.
- `app/src/main/java/org/ole/planet/myplanet/lite/TeamMember.kt`: Data class used only by the two unused classes above.

### Potential False Positives (Flagged but NOT recommended for removal)
- Many DTO classes in `DashboardTeamsRepository.kt`, `DashboardCoursesRepository.kt`, etc., appear to be used only within those files for JSON parsing. These are required for Moshi/Retrofit and should be kept.
- `GenderConstants.kt`: Contains top-level constants used in `SignupActivity` and `ProfileActivity`. Flagged initially because the filename itself isn't referenced, but the constants are.

## 2. XML Resources Dead Code

### Unused Drawables
The following drawables are not referenced in any layout, menu, or code.
- `app/src/main/res/drawable/dashboard_course_price_background.xml`
- `app/src/main/res/drawable/ic_fullscreen_24.xml`
- `app/src/main/res/drawable/icon_ole.xml`

### Unused Strings
The following strings in `strings.xml` (and their corresponding translations) are not referenced. Most appear to be remnants of mock data or deprecated features.
- `server_configuration_add_button`
- `dashboard_outbox_send_placeholder`
- `dashboard_outbox_back`
- `signup_level_option_beginner`
- `signup_level_option_intermediate`
- `signup_level_option_advanced`
- `signup_level_option_expert`
- `permission_required_message`
- `dashboard_courses_sample_name_creativity`
- `dashboard_courses_sample_name_digital_marketing`
- `dashboard_courses_sample_name_leadership`
- `dashboard_courses_sample_name_data`
- `dashboard_courses_sample_name_communication`
- `dashboard_courses_sample_name_finance`
- `dashboard_courses_sample_name_english`
- `dashboard_courses_sample_name_coding`
- `dashboard_courses_category_uiux`
- `dashboard_courses_category_development`
- `dashboard_courses_category_basic`
- `dashboard_courses_author_format`
- `dashboard_courses_price_format`
- `dashboard_course_lessons_summary`
- `dashboard_course_lessons_progress`
- `dashboard_course_lesson_duration`
- `dashboard_course_lessons_topic_intro`
- `dashboard_course_lessons_topic_why`
- `dashboard_course_lessons_topic_definitions`
- `dashboard_course_lessons_topic_types`
- `dashboard_course_lessons_topic_basics`
- `course_wizard_close`
- `course_wizard_fullscreen`
- `dashboard_course_details_description`

### Unused Colors
- `grayOle`
- `cyanOle`
- `login_hint`
- `login_input_border`
- `C919191`

### Unused Dimens
- `dashboard_scroll_bottom_padding`
- `dashboard_column_spacing`
- `dashboard_fab_bottom_offset`

## 3. Gradle Dependencies

### Unused Dependencies
- `com.github.bumptech.glide:glide`: The project uses custom image loaders (`DashboardAvatarLoader`, `DashboardPostImageLoader`) using OkHttp and LruCache instead of Glide.
- `de.hdodenhof:circleimageview`: No usages found in layouts or code.

### Dependencies Kept
- `org.json:json`: Although flagged as potentially unused in tests, it is required for JVM-based unit tests because the code under test (specifically `NetworkAuthService`) uses `JSONObject`, which is part of the Android SDK but not available in a standard JVM environment.

## 4. Reflection and Dynamic Loading
- **Moshi DTOs**: Use reflection for JSON serialization. They are kept as they are part of active Repositories.
- **View Binding**: Layouts are referenced via generated binding classes. My analysis accounted for this.
- **Activities/Manifest**: Entry points are checked against `AndroidManifest.xml`.
