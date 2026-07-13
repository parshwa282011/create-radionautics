-- Create: Radionautics helper API.
-- This wraps the radionautics_radio and radionautics_mega_radio peripherals with names that are pleasant to
-- autocomplete inside CC:Tweaked's editor.

local radionautics = {}

local function radio(side)
    local wrapped
    if side then
        wrapped = peripheral.wrap(side)
    else
        wrapped = peripheral.find("radionautics_radio") or peripheral.find("radionautics_mega_radio")
    end
    if not wrapped then error("No Radionautics radio peripheral attached", 2) end
    return wrapped
end

function radionautics.find()
    return radio()
end

function radionautics.udpBind(frequency, side)
    return radio(side).udpBind(frequency)
end

function radionautics.tcpListen(frequency, side)
    return radio(side).tcpListen(frequency)
end

function radionautics.close(frequency, side)
    return radio(side).close(frequency)
end

function radionautics.send(frequency, payload, side)
    return radio(side).send(frequency, payload)
end

function radionautics.sendMany(frequencies, payload, side)
    local wrapped = radio(side)
    if not wrapped.sendMany then error("sendMany requires a radionautics_mega_radio peripheral", 2) end
    return wrapped.sendMany(frequencies, payload)
end

function radionautics.receive(side)
    return radio(side).receive()
end

function radionautics.boundFrequencies(side)
    return radio(side).boundFrequencies()
end

function radionautics.queuedMessages(side)
    return radio(side).queuedMessages()
end

radionautics.crypto = {}

function radionautics.crypto.encrypt(kind, data, key, side)
    return radio(side).cryptoEncrypt(kind, data, key)
end

function radionautics.crypto.decrypt(kind, data, key, side)
    return radio(side).cryptoDecrypt(kind, data, key)
end

function radionautics.crypto.encode(kind, data, side)
    return radio(side).cryptoEncode(kind, data)
end

function radionautics.crypto.decode(kind, data, side)
    return radio(side).cryptoDecode(kind, data)
end

radionautics.sable = {}

function radionautics.sable.continousLoad(enabled, side)
    return radio(side).sableContinousLoad(enabled)
end

function radionautics.sable.isContinousLoading(side)
    return radio(side).sableIsContinousLoading()
end

function radionautics.sable.shipCenterPos(side)
    return radio(side).sableShipCenterPos()
end

return radionautics
