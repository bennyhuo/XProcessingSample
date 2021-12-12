# Mixin

This is an annotation processor to mix Java or Kotlin Classes up into a single Class. 

This is also a sample of [X Processing](https://androidx.tech/artifacts/room/room-compiler-processing/) which is an abstract layer of apt and ksp.

## Usage

The design of this project is very straightforward.

Suppose I have two api classes for User:

```kotlin
class UserApi {
    fun getName(id: Long): String = TODO()

    fun getAge(id: Long): Int = TODO()
}
```

```java
public class GitHubUserApi {
    public String getGitHubUrl(long id) {
        throw new NotImplementedError();
    }

    public int getRepositoryCount(long id) {
        throw new NotImplementedError();
    }
}
```

and I want to combine them into a single class `UserApis`, I will annotate these two classes with `@Mixin`:

```kotlin
@Mixin("com.bennyhuo.kotlin.mixin.sample", "UserApis")
class UserApi { 
    ... 
}
```

```java
@Mixin(packageName = "com.bennyhuo.kotlin.mixin.sample", name = "UserApis")
public class GitHubUserApi {
    ...
}
```

with apt or ksp, `com.bennyhuo.kotlin.mixin.sample.UserApis` will be generated: 

```java
package com.bennyhuo.kotlin.mixin.sample;

import com.bennyhuo.kotlin.mixin.sample.library1.UserApi;
import com.bennyhuo.kotlin.mixin.sample.library2.GitHubUserApi;
import java.lang.String;
import org.jetbrains.annotations.NotNull;

public class UserApis {
  private final UserApi userApi;

  private final GitHubUserApi gitHubUserApi;

  public UserApis() {
    userApi = new UserApi();
    gitHubUserApi = new GitHubUserApi();
  }

  public int getRepositoryCount(long id) {
    return gitHubUserApi.getRepositoryCount(id);
  }

  public String getGitHubUrl(long id) {
    return gitHubUserApi.getGitHubUrl(id);
  }

  @NotNull("")
  public String getName(long id) {
    return userApi.getName(id);
  }

  public int getAge(long id) {
    return userApi.getAge(id);
  }
}
```

That's it. 

It is pretty useful in some cases. 

For example, the JavaScript bridge for Android WebView. You may want to separate different bridge functions into different modules and add them to the WebView as one bridge object, so you have to write a wrapper class in the main module to assemble all the bridge functions. With this library, you can easily achieve that.

## Set up your project

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.6.0-1.0.1" // ksp
    id("org.jetbrains.kotlin.kapt") // kapt
}

repositories {
    mavenCentral()
}

dependencies {
    ksp("com.bennyhuo.kotlin:mixin-compiler:1.6.0.0") // ksp
    kapt("com.bennyhuo.kotlin:mixin-compiler:1.6.0.0") // kapt
    implementation("com.bennyhuo.kotlin:mixin-annotations:1.6.0.0")
}
```

# License

[MIT License](https://github.com/enbandari/KotlinDeepCopy/blob/master/LICENSE)

    Copyright (c) 2021 Bennyhuo
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

