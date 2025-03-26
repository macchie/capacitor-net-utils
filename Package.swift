// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NetUtils",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "NetUtils",
            targets: ["NetUtilsPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "NetUtilsPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NetUtilsPlugin"),
        .testTarget(
            name: "NetUtilsPluginTests",
            dependencies: ["NetUtilsPlugin"],
            path: "ios/Tests/NetUtilsPluginTests")
    ]
)