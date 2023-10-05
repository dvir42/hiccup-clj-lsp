# hiccup-clj-lsp

An LSP for writing html in clojure using the hiccup library 

## Installation

Using babahska, run `bb build`.

## Usage

For Emacs, add the following to your `init.el`:

```cl
(use-package lsp-mode
  :config
  (add-to-list 'lsp-language-id-configuration
               '(clojure-mode . "clojure"))
  (lsp-register-client
   (make-lsp-client :new-connection (lsp-stdio-connection "hiccup-clj-lsp")
                    :activation-fn (lsp-activate-on "clojure")
                    :server-id 'hiccup-clj-lsp
                    :add-on? t))
```

Note that the executable `hiccup-clj-lsp` must be in Emacs's PATH.
