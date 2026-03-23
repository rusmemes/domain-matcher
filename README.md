# Domain Matcher

A lightweight and efficient Kotlin utility for matching domains against a predefined set of patterns.

## 🚀 Features

- Fast domain matching using a trie-like structure
- Case-insensitive matching
- Supports subdomain matching
- Handles URLs with:
  - `http://` / `https://`
  - `www.` prefix
  - paths and query parameters
- Optional caching support
- Strict validation of input URLs

## 📦 Installation

Just copy the `DomainMatcher.kt` file into your project.

No external dependencies required.

## 🧠 How It Works

The matcher builds an internal tree structure from domain patterns.  
Domains are split into parts and processed in reverse order (from TLD to subdomain).

Example: calendar.google.com → ["com", "google", "calendar"]


This allows efficient matching of nested domains and subdomains.

## ✨ Usage

### Create matcher

```kotlin
val matcher = DomainMatcher.create(
    listOf(
        "calendar.google.com",
        "car.google.com"
    )
)
```

### Check domain

```kotlin
if (matcher matched "https://car.google.com/page") {
    println("Matched!")
}
```

### 🔍 Matching Rules
- Matching is case-insensitive
- Subdomains are supported:
```text
pattern: car.google.com
matches:
  ✔ car.google.com
  ✔ supercar.google.com
```
- Partial matches are NOT allowed:
```test
pattern: car.google.com
does NOT match:
  ✘ ar.google.com
  ✘ google.com
```

### ⚡ Performance
- Optimized for repeated lookups
- Optional caching interface:
```kotlin
interface Cache {
    operator fun get(url: String): List<String>?
    operator fun set(url: String, value: List<String>)
}
```
