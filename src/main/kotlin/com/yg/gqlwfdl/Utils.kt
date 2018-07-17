package com.yg.gqlwfdl

// Miscellaneous utility functions

/**
 * If the passed in [Iterable] has [any][Iterable.any] values in it, this calls the specified function [block] with `this`
 * value as its argument and returns its result. Otherwise returns null.
 *
 * @see [let]
 */
inline fun <T, R> Iterable<T>.letIfAny(block: (Iterable<T>) -> R): R? =
        if (this.any()) this.let { block(this) } else null