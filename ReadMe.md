 │ Task                               │ Purpose                      │                                                                                  
 ├────────────────────────────────────┼──────────────────────────────┤                                                                                  
 │ ./gradlew ktlintCheck              │ Check code style compliance  │                                                                                  
 ├────────────────────────────────────┼──────────────────────────────┤                                                                                  
 │ ./gradlew ktlintFormat             │ Auto-format all Kotlin files │                                                                                  
 ├────────────────────────────────────┼──────────────────────────────┤                                                                                  
 │ ./gradlew ktlintMainSourceSetCheck │ Check only main source       │                                                                                  
 ├────────────────────────────────────┼──────────────────────────────┤                                                                                  
 │ ./gradlew ktlintKotlinScriptCheck  │ Check build scripts (*.kts)  |
 ├────────────────────────────────────┼──────────────────────────────┤


 ./gradlew installDebug    ---  install in debug mode
 ./gradlew assembleRelease    ---  build release version
 ./gradlew assembleDebug    ---  build debug version
 ./gradlew clean --rerun-tasks   --- clean build