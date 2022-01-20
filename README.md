# JsonLogic

[![Clojars Project](https://img.shields.io/clojars/v/com.nytimes/jsonlogic.svg)](https://clojars.org/com.nytimes/jsonlogic)
[![Build Status](https://cloud.drone.io/api/badges/nytimes/jsonlogic/status.svg?ref=refs/heads/main)](https://cloud.drone.io/nytimes/jsonlogic)

A [jsonlogic][1] implementation for Clojure.

## Installation

``` clojure
[com.nytimes/jsonlogic "$VERSION"]
```

## Usage

``` clojure
(require '[nytimes.jsonlogic :as jsonlogic])

(jsonlogic/apply {:if [true "Foo" "Bar"]})
"Foo"
```

[1]: https://jsonlogic.com/
