#!/usr/bin/env python3

"""A tool for formatting code consistently."""

from argparse import ArgumentParser, Namespace
from enum import Enum, unique
from pathlib import Path
from re import compile as compile_pattern
from typing import (
    Final, FrozenSet, Generator, List, Literal, Match, Optional, Pattern,
    TextIO, cast,
)


@unique
class EOL(Enum):
    """End of line."""

    CRLF = ('\r\n')
    LF = ('\n')

    @classmethod
    def from_name(cls, name: Literal['CRLF', 'LF']) -> 'EOL':
        """Get an EOL from its name."""
        return cls[name]

    @classmethod
    def from_name_to_value(
            cls, name: Literal['CRLF', 'LF']) -> Literal['\r\n', '\n']:
        """Convert an EOL name to its value."""
        return cls[name].value


RE_TRAILING_WHITESPACE: Final[Pattern[str]] = compile_pattern(r'\s+$')
RE_TABS: Final[Pattern[str]] = compile_pattern(r'\t')
EXCLUDE: Final[FrozenSet[str]] = frozenset((
    '.git', '.mypy_cache', '__python__', 'target'))
KEEP_TABS: Final[FrozenSet] = frozenset(('Makefile', 'makefile'))
TAB_WIDTH: Final[int] = 4
DEFAULT_EOL: Final[EOL] = EOL.CRLF
parser: Final[ArgumentParser] = ArgumentParser(description=__doc__)
parser.add_argument(
    'target',
    nargs='?',
    default=Path.cwd(),
    type=Path,
    help=f"""The path to the target directory. Defaults to the current working
    directory ({str(Path.cwd())!r}).""",
)
parser.add_argument(
    '-e', '--eol',
    choices=('CRLF', 'LF'),
    help=fr"""The end of line character(s)to use.  Either {EOL.CRLF.name!r} for
    carriage-return line-feed ({EOL.CRLF.value!r}) or {EOL.LF.name!r} for
    line-feed ({EOL.LF.value!r}). The default is {DEFAULT_EOL.name!r}.""",
)


def main() -> None:
    """Clean the files in the target directory (recursive)."""
    args: Namespace = parser.parse_args()
    target: Path = cast(Path, args.target)
    opt_eol: Optional[str] = cast(Optional[str], args.eol)
    eol: EOL = DEFAULT_EOL if opt_eol is None else EOL[opt_eol]
    filepath: Path
    for filepath in iter_filepaths(target):
        clean(filepath, eol.value)


def iter_filepaths(dirpath: Path) -> Generator[Path, None, None]:
    """Recursively iterate over the files in a direcory."""
    path: Path
    for path in dirpath.glob('*'):
        if path.name in EXCLUDE:
            continue
        elif path.is_dir():
            yield from iter_filepaths(path)
        else:
            yield path


def clean(filepath: Path, eol: str) -> None:
    """Clean lines and end each with EOL."""
    eof_slice: Final[slice] = slice(-len(eol))
    file: TextIO
    text: str
    keep_tabs: bool = filepath in KEEP_TABS
    with filepath.open('r') as file:
        try:
            text = eol.join(
                clean_line(line, keep_tabs) for line in file.readlines())
        except UnicodeError:
            print(f'{filepath=}')
            raise
    if text[eof_slice] != eol:  # Make sure file ends with EOL.
        text += eol
    with filepath.open('w') as file:
        file.write(text)


def clean_line(original: str, replace_tabs: bool) -> str:
    """Clean trailing whitespace and (opt) replace tabs with spaces."""
    line: str = RE_TRAILING_WHITESPACE.sub('', original)
    if not replace_tabs:
        return line
    start: int
    stop: int
    last_stop: int = 0
    line_parts: List[str] = []
    match: Match[str]
    for match in RE_TABS.finditer(line):
        start, stop = match.span()
        width = TAB_WIDTH - (start-last_stop) % TAB_WIDTH
        line_parts += [line[last_stop:start], ' '*width]
        last_stop = stop
    line_parts += [line[last_stop:]]
    return ''.join(line_parts)


if __name__ == "__main__":
    main()
