// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/DatepollSystems/WaiterRobot-Shared-Android/org/datepollsystems/waiterrobot/shared-kmmbridge/1.6.10/shared-kmmbridge-1.6.10.zip"
let remoteKotlinChecksum = "48e04ad37f1c219d3e0295960af77e955004c3edad131cbedf6d4f4a295b0ba8"
let packageName = "shared"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
        ,
    ]
)