# Bitbucket Issue Release Note Converter

Converting closed issues release notes is a pain. This script uses the [Bitbucket API](https://confluence.atlassian.com/display/BITBUCKET/Using+the+Bitbucket+REST+APIs) to generate a markdown file of all the closed issues for a given milestone.

It prints to standard out so you can just redirect to a file and then add more info  in your favourite editor. 

## Usage

`release-note-genetor.py` takes three default arguments: bitbucket username, repository slug, and milestone.
 
`python release-note-generator.py -h` will show help.

Basic Usage is as follows:

    $ python release-note-generator.py themystic bitbucket-release-notes-generator 0.0.1
    - [1](https://bitbucket.org/themystic/bitbucket-release-notes-generator/issue/1) issue title displays here 
    - [2](https://bitbucket.org/themystic/bitbucket-release-notes-generator/issue/2) issue title displays here 
    - [3](https://bitbucket.org/themystic/bitbucket-release-notes-generator/issue/3) issue title displays here 


## License

This script is licensed under the MIT license. See the LICENSE file for details.
