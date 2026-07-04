# Create: Radionautics Design

Version: 0.1 draft

## Identity

- Display name: Create: Radionautics
- Jar base name: create_radionautics
- Mod id and registry namespace: create_radio
- Java package: com.parshwa.create.radionautics
- Minecraft: 1.21.1
- Loader: NeoForge

## Dependencies

Required:

- Create
- Create Aeronautics

Optional integrations:

- CC:Tweaked
- Create Radar, later

Create Radar is not required for v0.1, but the radio API reserves a
`radio.radar` namespace for future radar discovery and targeting.

## Blocks

### Brass Radio Link

The Brass Radio Link is the upgraded Create Redstone Link tier.

Behavior:

- Infinite range.
- Same dimension only.
- Sender/receiver mode, toggled by wrench/shift interaction and by GUI.
- Sends exact analog redstone strength, 0-15.
- Sends when the source strength changes.
- Supports text frequency configuration.
- Supports Create-style item frequency slots as a future-compatible config option.
- Supports optional encryption.
- When encrypted, the plaintext payload is `id_strength`, where `id` is a
  configurable shared identifier and `strength` is the redstone level.
- Receivers decrypt and only apply the signal if the shared id matches.
- Pulse and toggle modes are deferred.

Chunk/ship loading:

- Receivers should load a 5x5 chunk area when not on a Sable/Create Aeronautics
  ship, unless disabled by server config.
- If mounted on a Sable/Create Aeronautics ship, the link should keep the
  whole stable ship logically ticking, unless disabled by server config.
- Loading is for server-side logic only, never rendering.

### Radio Antennas

Radio antennas are CC:Tweaked peripherals when CC:Tweaked is installed. They
are block peripherals placed next to computers. Pocket computer support should
use a slot/upgrade path when the CC:Tweaked API wiring is implemented.

Tiers:

- Andesite Radio Antenna: 2000 block range, 1 active bound frequency.
- Copper Radio Antenna: 5000 block range, 2 active bound frequencies.
- Brass Radio Antenna: infinite range, 5 active bound frequencies.

Range:

- If both radios are normal world blocks, range is measured between block
  positions.
- If one or both radios are on Sable/Create Aeronautics ships, use the ship API
  true world position and calculate distance from that.
- A higher-tier antenna may talk to a lower-tier antenna only when the pair is
  within the lower-tier antenna's range.
- Communication is same dimension only for v0.1.

## CC:Tweaked API

There is no beginner modem-style API. Radios expose a socket/websocket-inspired
API.

The API is manual-protocol-first:

- Packets are binary internally.
- Users include packet identifiers manually in their payload.
- Encryption/decryption is explicitly chosen by user code.
- One server can bind a given frequency per radio/computer.
- Clients may still connect/send on a frequency that is also server-bound.
- Bind limits count active bound frequencies, not connected clients.

Suggested Lua shape:

```lua
local server = radio.udpBind("145.500")

server.onConnect(function(socket)
  socket.onData(function(data)
    if data == "chat:hello" then
      socket.send("chat:accepted")
    end
  end)
end)
```

Crypto helpers:

```lua
local encrypted = radio.crypto.encrypt("aes", data, key)
local clear = radio.crypto.decrypt("aes", encrypted, key)
```

Initial helper algorithms:

- none
- xor
- aes
- base64 encode/decode

Sable/Create Aeronautics helpers:

```lua
radio.sable.continousLoad(true)
radio.sable.continousLoad(false)
local pos = radio.sable.shipCenterPos()
```

The spelling `continousLoad` is intentional for the public Lua API.

Callbacks only run while the computer is awake and running. If the radio is
unloaded and continuous loading is not enabled, callbacks do not process.

## Chunk And Ship Loading

Continuous loading is player-controlled through Lua for radio antennas:

```lua
radio.sable.continousLoad(true)
```

Server config may disable this globally. Anyone may call it. It is a persistent
toggle until disabled, but does not persist radio packets across server restart.

## Packet Handling

User-facing packet size is unlimited. Internally, the mod should split large
payloads into reasonable chunks and reassemble them before delivery. This is a
transport implementation detail and must not change the Lua-facing message.

If two messages are sent at the same time, ordering is allowed to be arbitrary.

## Recipes And Progression

The mod is late-game oriented. The Brass Radio Link is mid-game, while radio
antennas are more complex and scale upward by tier.

Recipe direction:

- Andesite antenna: andesite alloy, copper, electron tube style components.
- Copper antenna: copper casing, electron tube, precision mechanism.
- Brass antenna: brass casing, precision mechanism, redstone link/radio parts.
- Brass Radio Link: complex Create recipe using brass, precision mechanism,
  redstone link, and radio components.

## CC:Tweaked Modem Replacement

When possible, CC:Tweaked wireless modems should be removed or disabled. If
direct removal is unsafe or unavailable, the mod should at least remove/disable
their recipes through data/compat hooks.

## Deferred

- Radio Console
- Communications Tower
- Cross-dimensional radio
- Pulse/toggle redstone modes
- Create Radar implementation
- Autopilot/GPS/weapon/terrain-scanning features
