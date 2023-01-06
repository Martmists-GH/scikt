# SciKt

An attempt to port numpy/scipy to Kotlin Multiplatform.

## Why?

I had an NDArray implementation in my commons repository, but I wanted to make it more general and usable. 
I also wanted to make it more performant, and I wanted to make it more usable in a multiplatform environment.
At that point, I thought it'd be a fun exercise in algorithms and optimization to port all of numpy and scipy to Kotlin.

## Architecture

The project is split into four subprojects:

- `numkt` - Which aims to port numpy features to Kotlin
- `scikt` - Which builds on top of `numkt` to port scipy features to Kotlin
- `union-annotations` and `union-processor` - Which are used to generate the union functions in `numkt` and `scikt`. These are useful when the output of a function may be ambiguous. They're a bit of a hack, but they work.

## Status

The project is still in its infancy, but should be usable for basic operations. I would not recommend using it for anything serious.

## Contributing

Contributions are welcome! If you want to contribute, submit a PR and I'll review it ~~as soon as I can~~ when I feel like it.

## License

This project is licensed under the BSD 3-Clause License. See the LICENSE file for more details.
