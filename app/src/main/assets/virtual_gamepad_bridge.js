/**
 * HomeArcade Virtual Gamepad W3C Bridge
 * Injects virtual gamepad slots (Player 2, 3, 4) into the W3C Gamepad API (navigator.getGamepads)
 * Compatible with EmulatorJS (WASM) and WebRTC Stream Player.
 */
(function() {
    if (window.__homeArcadeVirtualGamepadShim) return;
    window.__homeArcadeVirtualGamepadShim = true;

    // Slots 0: P1, 1: P2, 2: P3, 3: P4
    window.__virtualGamepads = [null, null, null, null];

    function createGamepadObject(slotIndex, buttonsMask, axes) {
        var buttons = [];
        for (var i = 0; i < 17; i++) {
            var isPressed = ((buttonsMask & (1 << i)) !== 0);
            buttons.push({
                pressed: isPressed,
                value: isPressed ? 1.0 : 0.0,
                touched: isPressed
            });
        }
        return {
            id: "HomeArcade Mobile Virtual Gamepad (Slot " + (slotIndex + 1) + ")",
            index: slotIndex,
            connected: true,
            timestamp: performance.now(),
            mapping: "standard",
            axes: axes || [0.0, 0.0, 0.0, 0.0],
            buttons: buttons
        };
    }

    var nativeGetGamepads = navigator.getGamepads ? navigator.getGamepads.bind(navigator) : function() { return []; };

    navigator.getGamepads = function() {
        var realGamepads = nativeGetGamepads() || [];
        var merged = [];
        for (var slot = 0; slot < 4; slot++) {
            if (window.__virtualGamepads[slot]) {
                // Use virtual gamepad from mobile device for this slot
                merged[slot] = window.__virtualGamepads[slot];
            } else if (realGamepads[slot]) {
                // Keep real physical gamepad connected to TV
                merged[slot] = realGamepads[slot];
            } else {
                merged[slot] = null;
            }
        }
        return merged;
    };

    window.__updateVirtualGamepad = function(slot, buttonsMask, axisX, axisY) {
        if (slot < 0 || slot >= 4) return;
        var wasConnected = (window.__virtualGamepads[slot] !== null);
        var gp = createGamepadObject(slot, buttonsMask, [axisX || 0.0, axisY || 0.0, 0.0, 0.0]);
        window.__virtualGamepads[slot] = gp;

        if (!wasConnected) {
            try {
                window.dispatchEvent(new GamepadEvent('gamepadconnected', { gamepad: gp }));
            } catch (e) {
                // Fallback for older WebView versions
                var evt = document.createEvent('CustomEvent');
                evt.initCustomEvent('gamepadconnected', true, true, { gamepad: gp });
                window.dispatchEvent(evt);
            }
        }
    };

    window.__disconnectVirtualGamepad = function(slot) {
        if (slot < 0 || slot >= 4) return;
        var old = window.__virtualGamepads[slot];
        if (old) {
            window.__virtualGamepads[slot] = null;
            try {
                window.dispatchEvent(new GamepadEvent('gamepaddisconnected', { gamepad: old }));
            } catch (e) {
                var evt = document.createEvent('CustomEvent');
                evt.initCustomEvent('gamepaddisconnected', true, true, { gamepad: old });
                window.dispatchEvent(evt);
            }
        }
    };
})();
