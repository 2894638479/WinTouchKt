package sendInput

import logger.info

enum class Keys(val code: UByte,val extraName: String? = null) {
    HIDE_SHOW(0x00u),
    LBUTTON(0x01u),
    RBUTTON(0x02u),
    CANCEL(0x03u),
    MBUTTON(0x04u),
    XBUTTON1(0x05u),
    XBUTTON2(0x06u),

    BACK(0x08u),
    TAB(0x09u),

    CLEAR(0x0Cu),
    RETURN(0x0Du),

    SHIFT(0x10u),
    CONTROL(0x11u),
    MENU(0x12u),
    PAUSE(0x13u),
    CAPITAL(0x14u),
    KANA_HANGUEL_HANGUL(0x15u),
    JUNJA(0x17u),
    FINAL(0x18u),
    HANJA_KANJI(0x19u),
    ESCAPE(0x1Bu),
    CONVERT(0x1Cu),
    NONCONVERT(0x1Du),
    ACCEPT(0x1Eu),
    MODECHANGE(0x1Fu),

    SPACE(0x20u),
    PRIOR(0x21u),
    NEXT(0x22u),
    END(0x23u),
    HOME(0x24u),
    LEFT(0x25u),
    UP(0x26u),
    RIGHT(0x27u),
    DOWN(0x28u),
    SELECT(0x29u),
    PRINT(0x2Au),
    EXECUTE(0x2Bu),
    SNAPSHOT(0x2Cu),
    INSERT(0x2Du),
    DELETE(0x2Eu),
    HELP(0x2Fu),

    `0`(0x30u), `1`(0x31u), `2`(0x32u), `3`(0x33u), `4`(0x34u),
    `5`(0x35u), `6`(0x36u), `7`(0x37u), `8`(0x38u), `9`(0x39u),

    A(0x41u), B(0x42u), C(0x43u), D(0x44u), E(0x45u),
    F(0x46u), G(0x47u), H(0x48u), I(0x49u), J(0x4Au),
    K(0x4Bu), L(0x4Cu), M(0x4Du), N(0x4Eu), O(0x4Fu),
    P(0x50u), Q(0x51u), R(0x52u), S(0x53u), T(0x54u),
    U(0x55u), V(0x56u), W(0x57u), X(0x58u), Y(0x59u),
    Z(0x5Au),

    LWIN(0x5Bu),
    RWIN(0x5Cu),
    APPS(0x5Du),
    SLEEP(0x5Fu),

    NUMPAD0(0x60u), NUMPAD1(0x61u), NUMPAD2(0x62u), NUMPAD3(0x63u),
    NUMPAD4(0x64u), NUMPAD5(0x65u), NUMPAD6(0x66u), NUMPAD7(0x67u),
    NUMPAD8(0x68u), NUMPAD9(0x69u),

    MULTIPLY(0x6Au),
    ADD(0x6Bu),
    SEPARATOR(0x6Cu),
    SUBTRACT(0x6Du),
    DECIMAL(0x6Eu),
    DIVIDE(0x6Fu),

    F1(0x70u), F2(0x71u), F3(0x72u), F4(0x73u), F5(0x74u), F6(0x75u),
    F7(0x76u), F8(0x77u), F9(0x78u), F10(0x79u), F11(0x7Au), F12(0x7Bu),
    F13(0x7Cu), F14(0x7Du), F15(0x7Eu), F16(0x7Fu),
    F17(0x80u), F18(0x81u), F19(0x82u), F20(0x83u), F21(0x84u), F22(0x85u),
    F23(0x86u), F24(0x87u),

    NUMLOCK(0x90u),
    SCROLL(0x91u),

    LSHIFT(0xA0u),
    RSHIFT(0xA1u),
    LCONTROL(0xA2u),
    RCONTROL(0xA3u),
    LMENU(0xA4u),
    RMENU(0xA5u),

    BROWSER_BACK(0xA6u),
    BROWSER_FORWARD(0xA7u),
    BROWSER_REFRESH(0xA8u),
    BROWSER_STOP(0xA9u),
    BROWSER_SEARCH(0xAAu),
    BROWSER_FAVORITES(0xABu),
    BROWSER_HOME(0xACu),

    VOLUME_MUTE(0xADu),
    VOLUME_DOWN(0xAEu),
    VOLUME_UP(0xAFu),

    MEDIA_NEXT_TRACK(0xB0u),
    MEDIA_PREV_TRACK(0xB1u),
    MEDIA_STOP(0xB2u),
    MEDIA_PLAY_PAUSE(0xB3u),

    LAUNCH_MAIL(0xB4u),
    LAUNCH_MEDIA_SELECT(0xB5u),
    LAUNCH_APP1(0xB6u),
    LAUNCH_APP2(0xB7u),

    OEM_1(0xBAu, ";:"),
    OEM_PLUS(0xBBu, "=+"),
    OEM_COMMA(0xBCu, ",<"),
    OEM_MINUS(0xBDu, "-_"),
    OEM_PERIOD(0xBEu, ".>"),
    OEM_2(0xBFu, "/?"),
    OEM_3(0xC0u, "`~"),

    GAMEPAD_A(0xC3u),
    GAMEPAD_B(0xC4u),
    GAMEPAD_X(0xC5u),
    GAMEPAD_Y(0xC6u),
    GAMEPAD_RIGHT_SHOULDER(0xC7u),
    GAMEPAD_LEFT_SHOULDER(0xC8u),
    GAMEPAD_LEFT_TRIGGER(0xC9u),
    GAMEPAD_RIGHT_TRIGGER(0xCAu),
    GAMEPAD_DPAD_UP(0xCBu),
    GAMEPAD_DPAD_DOWN(0xCCu),
    GAMEPAD_DPAD_LEFT(0xCDu),
    GAMEPAD_DPAD_RIGHT(0xCEu),
    GAMEPAD_MENU(0xCFu),
    GAMEPAD_VIEW(0xD0u),
    GAMEPAD_LEFT_THUMBSTICK_BUTTON(0xD1u),
    GAMEPAD_RIGHT_THUMBSTICK_BUTTON(0xD2u),
    GAMEPAD_LEFT_THUMBSTICK_UP(0xD3u),
    GAMEPAD_LEFT_THUMBSTICK_DOWN(0xD4u),
    GAMEPAD_LEFT_THUMBSTICK_RIGHT(0xD5u),
    GAMEPAD_LEFT_THUMBSTICK_LEFT(0xD6u),
    GAMEPAD_RIGHT_THUMBSTICK_UP(0xD7u),
    GAMEPAD_RIGHT_THUMBSTICK_DOWN(0xD8u),
    GAMEPAD_RIGHT_THUMBSTICK_RIGHT(0xD9u),
    GAMEPAD_RIGHT_THUMBSTICK_LEFT(0xDAu),

    OEM_4(0xDBu, "[{"),
    OEM_5(0xDCu, "\\|"),
    OEM_6(0xDDu, "]}"),
    OEM_7(0xDEu, "'\""),
    OEM_8(0xDFu),

    OEM_102(0xE2u, "<> or \\|"),

    PROCESSKEY(0xE5u),
    PACKET(0xE7u),

    ATTN(0xF6u),
    CRSEL(0xF7u),
    EXSEL(0xF8u),
    EREOF(0xF9u),
    PLAY(0xFAu),
    ZOOM(0xFBu),
    NONAME(0xFCu),
    PA1(0xFDu),
    OEM_CLEAR(0xFEu),
    EXIT(0xFFu);

    companion object {
        init {
            require(Keys.entries.map { it.code }.toSet().size == Keys.entries.size) { "detected duplicate keys in enum Keys" }
        }
        private val names = entries.associate { it.code to (it.extraName ?: it.name) }
        fun name(code: UByte) = names[code] ?: code.toString()
    }
}
