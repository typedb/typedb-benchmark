local fileprefix = "logs/"

-- setup lines function
local function lines(file)
    return function ()
        return file:read("*L")
    end
end

-- setup logs map
-- automatically open new log files for trackers
local logs = setmetatable({}, {
    __index = function(table, key)
        local filename = fileprefix .. key:lower():gsub(" ", "_"):gsub(":", "-") .. ".log"
        local file = assert(io.open(filename, "w"))
        table[key] = file
        return file
    end
})

-- MAIN PROGRAM
-- read all input lines
local tracker
for line in lines(io.stdin) do
    local logline
    if line:find("%d%d:%d%d:%d%d.%d%d%d") then
        --tracker = line:match("%((.*)%)")
        local s, e = line:find("%s%-%s%(.*%)")
        if s then
            tracker = line:sub(s+4,e-1)
            logline = line:sub(e+1)
        else
            tracker = nil
            logline = line
        end
    else
        logline = line
    end
    if tracker then
        logs[tracker]:write(logline)
    else
        io.stdout:write(logline)
    end
end

-- close all log files
for k, v in pairs(logs) do
    v:close()
end