# JsonLogic

[![Clojars Project](https://img.shields.io/clojars/v/com.nytimes/jsonlogic.svg)](https://clojars.org/com.nytimes/jsonlogic)
[![Build Status](https://cloud.drone.io/api/badges/nytimes/jsonlogic/status.svg?ref=refs/heads/main)](https://cloud.drone.io/nytimes/jsonlogic)

A [jsonlogic][1] implementation for Clojure.

## Installation

```clojure
[com.nytimes/jsonlogic "$VERSION"]
```

## Usage

```clojure
(require '[nytimes.jsonlogic :as jsonlogic])

(jsonlogic/apply {:if [true "Foo" "Bar"]})
"Foo"
```

This library supports encoding the operators as keywords, string, or symbols.
All of these values are coerced to symbols for evalution..

```clojure
(jsonlogic/apply {"+" [1 2]})
;;=> 3
(jsonlogic/apply {:+ [1 2]})
;;=> 3
(jsonlogic/apply {'+ [1 2]})
;;=> 3
```

## Extending JsonLogic

You can add custom JsonLogic operations by extending the `operate` multi-method.

### Arguments

Operate receives two arguments, a map of the operation and the data provided to
apply. For example, given this invocation:

```clojure
(jsonlogic/apply {"*" [{"var" "x"} {"var" "x"}]}
                 {"x" 2})
```

The arguments to `*` are:

1. `{* [{var "x"} {var "x"}]}`
2. `{"x" 2}`

### A Custom Operator Example

Here is an example of adding a custom power function.

```clojure
(require '[com.nytimes.jsonlogic :as json])
(require '[clojure.math :as math])

(defmethod jsonlogic/operate '**
  [{args '**} _env]
  (if (not= 2 (count args))
    (throw (ex-info "** requires exactly two arguments" {:args args}))
    (let [[base exponent] args]
      (math/pow base exponent))))

(jsonlogic/apply {'** [2 3]})
;;=> 8.0
```

[1]: https://jsonlogic.com/
