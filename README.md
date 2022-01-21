# JsonLogic

A [jsonlogic][1] implementation for Clojure.

## Installation

``` clojure
[com.nytimes/jsonlogic "TBD"]
```

## Usage

``` clojure
(require '[nytimes.jsonlogic :as jsonlogic])

(jsonlogic/apply {:if [true "Foo" "Bar"]})
"Foo"
```

[1]: https://jsonlogic.com/
