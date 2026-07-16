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

The Ground Base appends the sender's exact projected world distance as the fifth
value of each `radio_message` event. This uses Sable's world projection for
antennas mounted on ships:

```lua
local event, frequency, senderId, message, distance = os.pullEvent("radio_message")
print(("Signal arrived from %.3f blocks away"):format(distance))
```

## Creative Radio Receiver

The Creative Radio Receiver is an uncraftable operator tool. It monitors clean
packet traffic on every frequency, at any range and across dimensions. Attach a
CC:Tweaked computer and listen for:

```lua
local event, frequency, senderId, message, distance, decodeMethod, raw, senderPos =
  os.pullEvent("radio_audit")
```

`decodeMethod` is `plain`, `base64`, or `radionautics_crypto`. AES/XOR messages
are automatically decoded when the sender created them with Radionautics'
`cryptoEncrypt` helper during the current server session. Independently created
encrypted ciphertext cannot be decoded without its key and remains available in
`raw` for auditing.

## Recorded Media

Normal packets still emit `radio_message`. Completed media uses a separate audio
or visual path and emits `radio_audio`, `radio_image`, or `radio_video`.

### Recording voice with Simple Voice Chat

Simple Voice Chat is optional for users but installed in the development run.
Start a recording for a consenting player's UUID, speak using that mod's normal
push-to-talk/voice activation, then stop to transmit the completed recording:

```lua
radio.startVoiceRecording("operations", "00000000-0000-0000-0000-000000000000")
-- Speak, then:
local receivers = radio.stopVoiceRecording()
```

The maximum recording length is five minutes. Audio arrives only after stopping:

```lua
local _, frequency, senderId, mediaId, format, _, _, duration, distance =
  os.pullEvent("radio_audio")
-- format is pcm_s16le_48000_mono. Audio is stored once on the server.
```

Audio remains in the shared server cache for ten minutes. Payload files are kept
once under the active world save's `media/create_radio/` directory, never inside
a CC computer directory. Only radios which were
eligible receivers when it was sent may list or read it; the creative audit
receiver may access every entry. Read it in chunks (maximum 128 KiB per call):

```lua
local info = radio.mediaInfo(mediaId)
local offset = 0
while offset < info.bytes do
  local chunk = radio.mediaRead(mediaId, offset, math.min(128 * 1024, info.bytes - offset))
  offset = offset + #chunk
  -- decode/play or otherwise process chunk
end

for _, item in ipairs(radio.mediaList()) do
  print(item.id, item.frequency, item.duration, item.expiresUtc)
end
```

Expired files are deleted after ten minutes. The cache is cleared when the server stops, rejects individual recordings over
32 MiB, and evicts the oldest recordings if its total reaches 256 MiB.

The cache directory is also wiped whenever the server starts, including orphaned
files left by a crash. Operators may clear it immediately with:

```text
/radionautics clear_media
```

`cancelVoiceRecording()` discards the active recording.

### Exposure: CMOS images

Exposure: CMOS emits `wireless_frame` with a palette identifier, a `{width,
height}` size table and a binary pixel buffer. Send that completed frame directly:

```lua
local _, _, palette, size, pixels = os.pullEvent("wireless_frame")
radio.sendImage("recon", pixels, tostring(palette), size[1], size[2])
```

The Ground Base receives it separately from text:

```lua
local _, frequency, senderId, pixels, palette, width, height, _, distance =
  os.pullEvent("radio_image")
```

### Displaying an image accurately

`prepareImage` resolves the real Exposure palette, downsamples by averaging the
covered source pixels, and optionally applies Floyd-Steinberg dithering into the
CC:Tweaked 16-color palette:

```lua
local monitor = assert(peripheral.find("monitor"), "Attach a monitor")
monitor.setTextScale(0.5)
local mw, mh = monitor.getSize()
local rows = radio.prepareImage(pixels, palette, width, height, mw, mh, true)

for y, colors in ipairs(rows) do
  monitor.setCursorPos(1, y)
  monitor.blit((" "):rep(#colors), colors, colors)
end
```

Passing `false` as the final argument disables dithering for a cleaner, flatter
look. The output rows are native CC blit-color strings.

### Printing an image

A CC printer is monochrome and limited to 25x21 characters, so
`preparePrintedImage` creates a luminance-shaded printable version:

```lua
local printer = assert(peripheral.find("printer"), "Attach a printer")
local rows = radio.preparePrintedImage(pixels, palette, width, height, 25, 21)

assert(printer.newPage(), "Printer needs paper and ink")
printer.setPageTitle("Radio image")
for y, row in ipairs(rows) do
  printer.setCursorPos(1, y)
  printer.write(row)
end
assert(printer.endPage(), "Could not finish page")
```

The resulting page can be removed from the printer and viewed or placed normally.

Completed custom video archives use:

```lua
radio.sendVideo("recon", videoBytes, "rvid", width, height, durationSeconds)
```

and arrive through `radio_video`. Media audit events are named
`radio_audio_audit`, `radio_image_audit`, and `radio_video_audit`.

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
