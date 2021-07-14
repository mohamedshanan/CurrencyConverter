**FRONTEND CHALLENGE**

**I. Add arithmetic operators (add, subtract, multiply, divide) to make the following expressions true. You can use any parentheses
you’d like. You don’t need to write any code for this question.**
3 1 3 9 = 12

**Solution**
(3 + (1/3)) + 9


**II. Write a function in Kotlin to determine whether two strings are anagrams or not (examples of anagrams: debit card/bad
credit, punishments/nine thumps, etc.)**

 fun isAnagram(str1: String, str2: String) = Arrays.equals(str1.chars().sorted().toArray(),
            str2.chars().sorted().toArray())


**III. Write a function in Kotlin to generate the nth Fibonacci number (1, 1, 2, 3, 5, 8, 13, 21, 34)**

A. recursive approach

tailrec fun fibonacci(n: Int, a: Int = 0, b: Int = 1): Int =
        when (n) {
            0 -> a
            1 -> b
            else -> fibonacci(n - 1, b, a + b)
        }


B. iterative approach

fun fibonacci(n: Long): Long {
        if (n < 2) return n
        var minusOne: Long = 1
        var minusTwo: Long = 0
        var result = minusOne
        for (i in 2..n) {
            result = minusOne + minusTwo
            minusTwo = minusOne
            minusOne = result
        }
        return result
    }

IV. Create a currency converter by utilizing data from the fixer.io API.
The currency converter must use EUR as the base currency (displayed at top) and display the currency rates as a list. When a user taps on
a currency, a calculation view should appear with the selected currency and the base currency. Only the base currency field should be
editable.
Feel free to use any open source libraries.
(Consider this project as if you were developing a component within a large-scaled project)

V. Please use stable versions for IDE used
VI. Please share your answers in a public repo in Github, and delete the task after we review your challenge. Best of luck!


NOTE: This project is based on Android Architecture Blueprints (https://github.com/googlesamples/android-architecture)
