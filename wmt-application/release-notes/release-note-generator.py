__author__ = 'Tom Aratyn <tom@aratyn.name>'

import argparse

import requests

API_BASE = "https://bitbucket.org/api/1.0/repositories"
WEBSITE_BASE = "https://bitbucket.org"


def create_arg_parser():
    parser = argparse.ArgumentParser(description='Generate release notes from bitbucket issues.')
    parser.add_argument("username", metavar="username", help="Username of the owner of the repo")
    parser.add_argument("repository_slug", metavar="repository-slug",
                        help="The repository you want to generate notes from")
    parser.add_argument("milestone", metavar="milestone",
                        help="This miletstone's issues will become the body of the release notes.")
    return parser


def convert_issue_resource_uri_to_website_url(resource_uri):
    _, _, _, username, repo, _, issue_id = resource_uri.split("/")
    return "%s/%s/%s/issue/%s/" % (WEBSITE_BASE, username, repo, issue_id)


def get_raw_issues(username, repo_slug, milestone, kind):
    url = "%s/%s/%s/issues?limit=50&status=resolved&kind=%s" % (API_BASE, username, repo_slug, kind)
    response = requests.get(url, params={"milestone": milestone})
    if response.status_code != 200:
        raise Exception("Problem getting raw issues: %s" % response.status_code)
    return response

def print_issues(response):
    for issue in [issue for issue in response.json()["issues"]]:
        issue_url = convert_issue_resource_uri_to_website_url(issue["resource_uri"])
        print " - [%d] %s (%s) " % (issue["local_id"], issue["title"], issue_url)    

def main():
    parser = create_arg_parser()
    args = parser.parse_args()
    username = args.username
    repo_slug = args.repository_slug.lower()
    milestone = args.milestone
    print "Fixed Bugs:"
    print_issues(get_raw_issues(username, repo_slug, milestone, "bug"))
    print "Enhancements:"
    print_issues(get_raw_issues(username, repo_slug, milestone, "enhancement"))
    print "Completed Tasks:"
    print_issues(get_raw_issues(username, repo_slug, milestone, "task"))
    print "Accepted Proposals:"
    print_issues(get_raw_issues(username, repo_slug, milestone, "proposal"))


if __name__ == "__main__":
    main()


