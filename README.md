# KPhoenix

*WORK IN PROGRESS* Not well written code, nor stable, just a working Proof-of-Concept.

Phoenix Framework Channels and Presence Client written in Kotlin Multiplatform. 

They goal is to undestand well enough Elixir and Phoenix Channels, create a flexible and configurable Phoenix Client in Kotlin Multiplatform, so we can develop Phoenix Channels applications with a single front-end codebase.

## Milestones

- [x] Create a working sample, mostly direct conversion from JS code, if possible almost no changes to the JS api, don't need to be well written, not even well understood, not tested at all, only a single platform is enough, just a working sample to get going
- [ ] Understand the codebase, try the poorly converted code to understand all of its codepaths. Expand the server sample to have more edge cases
- [ ] Clean the codebase, refactor to well written Kotlin, start writting tests, try to optimize code to serve just enough
- [ ] Make a well designed API to support multiple Serialization methods, multiplatform and platform-specific, make it support Kotlin Serializable, then Android/Java-specifics like Gson/Moshi and iOS-specific like Swift's Codable or SwiftJSON and such
- [ ] Expand Socket Client interface to be easily exchangeable, actually in the sample we already have 2 different Android Socket implementations, expand it further and support the same with iOS. Maybe use in the future a truly Kotlin Multiplatform Socket Client, but still maintaining API open to change default client through configuration, so the library user is free to use whatever socket client works best for them if they implement the right interface
- [ ] Try making it possible to also generate JS, so we can still also do Web Development in a single codebase, this is the last step, because as much I think it is worth, I don't want to stop development creativity with this requirement
