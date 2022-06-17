# gradle-version-catalog-converter
Convert `implementation 'androidx.core:core-ktx:1.7.0'` into `androidxCoreCoreKtx = { module = "androidx.core:core-ktx", version.ref = "androidxCore" }`


You can try it here.

https://takahirom.github.io/gradle-version-catalog-converter/

![gradle-version-catalog-converter](https://user-images.githubusercontent.com/1386930/173219381-487560e5-5c51-4611-8eb2-454033387936.gif)


## Development

The entire source code is here. You can modify it as you like, and I look forward to your PR.

https://github.com/takahirom/gradle-version-catalog-converter/blob/main/src/jsMain/kotlin/Main.kt


Deploy docs(Update /docs/index.html)  
`./gradlew assmeble`


Run in browser  
`./gradlew jsBrowserRun`
