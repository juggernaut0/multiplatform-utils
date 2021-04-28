;(function (config) {
    var resolve = config.resolve ? config.resolve : {};
    config.resolve = resolve;
    var fallback = resolve.fallback ? resolve.fallback : {};
    resolve.fallback = fallback;
    Object.assign(fallback, {
        "crypto": require.resolve("crypto-browserify"),
        "stream": require.resolve("stream-browserify"),
        "buffer": require.resolve("buffer/")
    });
})(config);
