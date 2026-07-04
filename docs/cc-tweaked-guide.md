# Create: Radionautics CC:Tweaked Guide

Create: Radionautics adds radio peripherals for CC:Tweaked computers. These radios let computers send messages over long distances using Create-themed antenna blocks.

## Requirements

- Minecraft 1.21.1
- NeoForge
- Create
- Create Aeronautics
- CC:Tweaked
- Create: Radionautics

## Radio Blocks

Radionautics adds three CC:Tweaked radio antenna peripherals:

| Block | Range | Active bound frequencies |
| --- | ---: | ---: |
| Andesite Radio Antenna | 2000 blocks | 1 |
| Copper Radio Antenna | 5000 blocks | 2 |
| Brass Radio Antenna | Infinite | 5 |

Place a radio antenna directly next to a CC:Tweaked computer.

## Getting The Peripheral

Use `peripheral.find`:

```lua
local radio = peripheral.find("radionautics_radio")
```

Or wrap a known side:

```lua
local radio = peripheral.wrap("left")
```

You can check the radio tier:

```lua
print(radio.tier())
print(radio.range())
print(radio.maxBinds())
```

## Sending A Message

Sender:

```lua
local radio = peripheral.find("radionautics_radio")

radio.send("145.500", "chat:hello from base")
```

The first argument is the frequency. The second argument is the message.

## Receiving Messages

Receiver:

```lua
local radio = peripheral.find("radionautics_radio")

radio.udpBind("145.500")

while true do
  local event, frequency, senderId, message = os.pullEvent("radio_message")
  print("Frequency: " .. frequency)
  print("Sender: " .. senderId)
  print("Message: " .. message)
end
```

The receiver must bind the frequency first:

```lua
radio.udpBind("145.500")
```

Incoming messages fire a `radio_message` event:

```lua
local event, frequency, senderId, message = os.pullEvent("radio_message")
```

## Chat Example

Run this on the receiving computer:

```lua
local radio = peripheral.find("radionautics_radio")
radio.udpBind("145.500")

print("Listening on 145.500")

while true do
  local event, frequency, senderId, message = os.pullEvent("radio_message")
  print("[" .. frequency .. "] " .. senderId .. ": " .. message)
end
```

Run this on the sending computer:

```lua
local radio = peripheral.find("radionautics_radio")

while true do
  write("Message: ")
  local message = read()
  radio.send("145.500", "chat:" .. message)
end
```

## Encryption Helpers

Radionautics includes helper methods for encoding/encryption.

Available crypto types:

- `none`
- `xor`
- `aes`
- `base64`

Encrypted sender:

```lua
local radio = peripheral.find("radionautics_radio")

local key = "secret-key"
local message = "chat:encrypted hello"
local encrypted = radio.cryptoEncrypt("aes", message, key)

radio.send("145.500", encrypted)
```

Encrypted receiver:

```lua
local radio = peripheral.find("radionautics_radio")

radio.udpBind("145.500")

local key = "secret-key"

while true do
  local event, frequency, senderId, encrypted = os.pullEvent("radio_message")
  local ok, message = pcall(function()
    return radio.cryptoDecrypt("aes", encrypted, key)
  end)

  if ok then
    print(message)
  end
end
```

If the wrong key is used, decrypting may fail. Use `pcall` if you want to ignore bad packets safely.

## Frequencies

Frequencies are text. They can be numbers or names:

```lua
radio.udpBind("145.500")
radio.udpBind("AIRSHIP")
radio.udpBind("main_base")
```

The sender and receiver must use the same frequency.

## Bind Limits

Each antenna tier has a maximum number of active bound frequencies.

```lua
radio.udpBind("145.500")
radio.udpBind("fleet")
```

If the antenna is out of bind slots, binding returns `false`.

Close a frequency when finished:

```lua
radio.close("145.500")
```

List active binds:

```lua
for _, frequency in ipairs(radio.boundFrequencies()) do
  print(frequency)
end
```

## Queued Messages

You can poll messages manually:

```lua
local packet = { radio.receive() }

if packet[1] then
  local frequency = packet[1]
  local senderId = packet[2]
  local message = packet[3]
  print(message)
end
```

Usually, `os.pullEvent("radio_message")` is easier.

Check queued message count:

```lua
print(radio.queuedMessages())
```

## Sable / Ship Helpers

Radionautics exposes early Sable/Create Aeronautics helper methods:

```lua
radio.sableContinousLoad(true)
radio.sableContinousLoad(false)
```

Check if continuous loading is enabled:

```lua
print(radio.sableIsContinousLoading())
```

Get the current ship center position:

```lua
local pos = radio.sableShipCenterPos()
print(pos.x, pos.y, pos.z)
```

Note: ship integration is still being developed. Current behavior may fall back to block position.

## Optional Helper API

Radionautics also provides a helper API:

```lua
local radio = radionautics.find()
radio.udpBind("145.500")
radio.send("145.500", "chat:hello")
```

Crypto helper:

```lua
local encrypted = radionautics.crypto.encrypt("aes", "hello", "key")
local clear = radionautics.crypto.decrypt("aes", encrypted, "key")
```

Sable helper:

```lua
radionautics.sable.continousLoad(true)
```

## Troubleshooting

### `No radionautics_radio peripheral attached`

Make sure the antenna block is directly next to the computer.

### No message received

Check:

- Receiver called `radio.udpBind(frequency)`.
- Sender and receiver use the exact same frequency.
- The antennas are in the same dimension.
- The antennas are within range.
- The receiver program is still running.

### Encrypted message fails

Check:

- Both computers use the same crypto type.
- Both computers use the same key.
- The receiver uses `pcall` if it wants to ignore bad packets.

### Bind fails

The antenna may be out of active frequency slots.

Use:

```lua
print(radio.maxBinds())
print(radio.boundFrequencies())
```

Then close unused frequencies:

```lua
radio.close("old_frequency")
```
