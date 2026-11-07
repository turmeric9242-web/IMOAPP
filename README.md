# IMO Prep (Class 7)

A self-study app for the SOF International Mathematics Olympiad, Class 7.
Chapter quizzes, a timed full mock test, study notes and saved progress.

## Get the app

1. Open the **Releases** page of this repo.
2. Download `app-release.apk` on your Android phone.
3. Allow installs from your browser or file manager when asked, then install.

Android 7.0 (API 25) or newer is required.

## Add more questions

All questions live in `app/src/main/assets/questions.json`. Each entry looks like this:

```json
{
  "id": "SP26-Q2",
  "section": "Logical Reasoning",
  "chapter": "Coding-Decoding",
  "text": "Question text here",
  "options": ["first", "second", "third", "fourth"],
  "answer": "B",
  "explanation": "Why B is correct."
}
```

- `answer` is the letter of the correct option (A, B, C or D).
- `chapter` must match a chapter name from the syllabus, or it appears as a new chapter in the quiz picker.
- Questions that depend on a figure are not supported yet. Add an image field if you need them.

Every push to `main` builds a new APK and publishes it under Releases.

## Updating an installed app

Release builds are signed with the debug key, which is generated on the build machine. If an update
says the app conflicts with the installed version, uninstall the old one first. Your saved progress will be lost.
