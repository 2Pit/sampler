package git

import java.util.*


fun String.base64toUtf8(): String {
    //GitHub put '\n' on each 61 position
    return String(Base64.getDecoder().decode(this.replace("\n", "")))
}