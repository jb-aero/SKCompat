SKCompat
========

An extension to [CommandHelper](https://github.com/sk89q/CommandHelper) providing access to features of other plugins in the sk89q family. Currently supports WorldEdit and WorldGuard.

Downloads can be found in [the Releases tab](https://github.com/jb-aero/SKCompat/releases).

## Documentation
**CommandHelper's documentation generator does not work, and people keep asking why I removed functions just because they were not listed here. I did not remove any functions. So now you have to check yourself.**

Add the following to main.ms to give yourself a /docs command that tabcompletes with functionnames and classes. For example, "/docs SKWorldGuard" will return a list of all the WorldGuard functions, but you can see all functions containing "sk" by typing "/docs sk" and hitting tab. "/docs sk_region_addmember" will give you the documentation for the sk_region_addmember function.

The below can also be found on the [forums](http://forum.enginehub.org/threads/jb_aeros-scripts.9347/#post-19243).
```
# I do this as a cache because there are a lot of functions,
# this way I only have to use get_functions() once
@gfuncs = get_functions()
export('gfuncs', @gfuncs)
export('funclasses', array_keys(@gfuncs))
@funcs = array()
foreach(@gfuncs, @fc, @fa,
    foreach(@fa, @f,
        array_push(@funcs, @f)
    )
)
export('funcs', @funcs)
 
register_command('docs', array(
    description: 'Shows the documentation for a given CommandHelper function',
    permission: 'commandhelper.*',
    noPermMsg: color(c).'This is not the command you are looking for',
    usage: colorize('&aUsage&f: /docs [FunctionType|function_name]\n&aExample&f: /docs\n&aExample&f: /docs PlayerManagement\n&aExample&f: /docs ploc'),
    aliases: array('doc', 'func', 'funcs'),
    executor: closure(@al, @p, @args, @cmd,
        @gfuncs = import('gfuncs')
        @fcs = import('funclasses')
        @funcs = import('funcs')
        if(array_size(@args) == 0) {
            tmsg(@p, colorize('&aFunctionTypes&f: &e'.array_implode(@fcs, '&f, &e')))
            return(false)
        } else if (array_size(@args) == 1) {
            @choice = to_lower(@args[0])
            foreach(@fcs, @fc,
                if (@choice == to_lower(@fc)) {
                    tmsg(@p, colorize('&6'.@fc.'&f: &b'.array_implode(@gfuncs[@fc], '&f, &b')))
                    return(true)
                }
            )
            if (array_contains(@funcs, @choice)) {
                tmsg(@p, colorize('&2'.@choice.'&f:'))
                tmsg(@p, colorize('&aReturn&f: '.reflect_docs(@choice, return)))
                tmsg(@p, colorize('&aArgs&f: '.reflect_docs(@choice, args)))
                tmsg(@p, colorize('&aDescription&f: '.reflect_docs(@choice, description)))
            } else {
                tmsg(@p, 'You entered \''.@choice.'\'')
                tmsg(@p, 'Use \'/docs\' to get a list of valid function names')
            }
        } else {
            return(false)
        }
    ),
    tabcompleter: closure(@al, @p, @args, @cmd,
        @funcs = import('funcs')
        @gfuncs = import('gfuncs')
        @fcs = import('funclasses')
        if(array_size(@args) == 1) {
            @c = to_lower(@args[0])
            @returnable = array()
            foreach(@fcs, @fc,
                if(string_position(to_lower(@fc), @c) != -1) {
                    array_push(@returnable, @fc)
                }
            )
            foreach(@funcs, @f,
                if(string_position(@f, @c) != -1) {
                    array_push(@returnable, @f)
                }
            )
            return(@returnable)
        } else {
            return(array())
        }
    )
))
```
