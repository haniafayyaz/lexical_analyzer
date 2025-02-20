22L-6941 (Hania Fayyaz)
22K-4656 (Fatima Hameed)

# Custom Programming Language

This repository defines a simple programming language with specific rules for identifiers, numbers, keywords, operators, and punctuators.

## **Introduction**
This programming language is designed with strict syntax rules. The language includes:
- A set of **keywords** for control flow and data types.
- Rules for defining **identifiers**.
- Support for **operators** and **punctuation symbols**.
- A defined format for **numeric values**.

This document outlines the syntax rules and their corresponding **regular expressions (regex)**.

---

## **1. Identifiers**
- Identifiers must consist **only of lowercase letters (`a-z`)**.
- **Numbers, uppercase letters, and special characters are not allowed.**
- **Examples:**
  - `variable`
  - `myvar`

**Regex Pattern:**
```regex
^[a-z]+$
```

---

## **2. Numbers**
- Numbers can be **integers** or **floating-point values**.
- Floating-point numbers can have up to **five decimal places**.
- **Examples:**
  - `123`
  - `3.14159`

**Regex Pattern:**
```regex
^?\d+(\.\d{1,5})?$
```

---

## **3. Keywords**
- The following words are **reserved keywords** and cannot be used as identifiers:
  ```
  for, if, else, return, void, main, int, bool, float, char
  ```
- **Examples:**
  -  `if (x > 0) { return x; }`

**Regex Pattern:**
```regex
^(for|if|else|return|void|main|int|bool|float|char)$
```

---

## **4. Operators**
- The language supports the following **operators**:
  ```
  +, -, *, /, %, =
  ```
- **Examples:**
  -  `a = b + c * 5;`
  -  `x = y % 2;`

**Regex Pattern:**
```regex
^[+\-*/%=]$
```

---

## **5. Punctuators**
- The following **punctuation symbols** are recognized:
  ```
  , ; { } [ ] ( )
  ```
- **Examples:**
  - `if (x > 0) { return x; }`

**Regex Pattern:**
```regex
^[,;{}\[\]()]$
```

---

## **Usage**
This language is designed for parsing and tokenization. You can use the provided **regex patterns** to validate different components of the language.

## **Example Code**
```c
int main() {
    int a = 10;
    if (a > 5) {
        return a;
    }
}
```
---

