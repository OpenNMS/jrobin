#!/usr/bin/env perl

use File::Spec;
use Getopt::Long qw(:config permute bundling pass_through);

use vars qw(
	$HELP
	$JAVA_HOME
	$MVN
	$MAVEN_OPTS
	$PREFIX
	$TESTS
	$VERBOSE
	@ARGS
);
$HELP       = undef;
$JAVA_HOME  = undef;
$TESTS      = undef;
$VERBOSE    = undef;
@ARGS       = ();

# path to maven executable
$MVN = $ENV{'MVN'};
if (not -x $MVN) {
	$MVN = $PREFIX . '/maven/bin/mvn';
}

if (not -x $MVN) {
	chmod(0755, $MVN);
}
# maven options
$MAVEN_OPTS = $ENV{'MAVEN_OPTS'};
if (not defined $MAVEN_OPTS or $MAVEN_OPTS eq '') {
	$MAVEN_OPTS = '-XX:PermSize=512m -XX:MaxPermSize=1g -Xmx1g';
}

my $result = GetOptions(
	"help|h"                    => \$HELP,
	"enable-tests|tests|test|t" => \$TESTS,
	"maven-opts|m=s"            => \$MAVEN_OPTS,
	"java-home|java|j=s"        => \$JAVA_HOME,
	"verbose|v"                 => \$VERBOSE,
);
if (not $result) {
	error("failed to parse command-line options");
}

@ARGS = @ARGV;

if (defined $HELP) {
	print <<END;
usage: $0 [-h] [-j \$JAVA_HOME] [-t] [-v]

	-h/--help              this help
	-j/--java-home DIR     set \$JAVA_HOME to DIR
	-m/--maven-opts OPTS   set \$MAVEN_OPTS to OPTS
	                       (default: $MAVEN_OPTS)
	-t/--enable-tests      enable tests when building
	-v/--verbose           be more verbose
END
	exit 1;
}

if (not defined $JAVA_HOME) {
	debug("--java-home not passed, searching for \$JAVA_HOME");
	if (exists $ENV{'JAVA_HOME'} and -e $ENV{'JAVA_HOME'}) {
		$JAVA_HOME = $ENV{'JAVA_HOME'};
	} else {
		warning("\$JAVA_HOME is not set, things might go wonky.  Or not.");
	}
}
$ENV{'JAVA_HOME'} = $JAVA_HOME;
info("JAVA_HOME = $JAVA_HOME") if (defined $JAVA_HOME and $JAVA_HOME ne "");

if (defined $TESTS) {
	debug("tests are enabled");
} else {
	debug("tests are not enabled, passing -Dmaven.test.skip.exec=true");
	unshift(@ARGS, '-Dmaven.test.skip.exec=true');
}
unshift(@ARGS, '-Djava.awt.headless=true');

if (grep { $_ =~ /^-Droot.dir=/ } @ARGS) {
	debug("root.dir defined");
} else {
	debug("setting root.dir to $PREFIX");
	unshift(@ARGS, '-Droot.dir=' . $PREFIX);
}

if (-r $ENV{'HOME'} . "/.opennms-buildrc") {
	if (open(FILEIN, $ENV{'HOME'} . "/.opennms-buildrc")) {
		while (my $line = <FILEIN>) {
			chomp($line);
			if ($line !~ /^\s*$/ && $line !~ /^\s*\#/) {
				unshift(@ARGS, $line);
			}
		}
		close(FILEIN);
	}
}

$ENV{'MAVEN_OPTS'} = $MAVEN_OPTS;
info("MAVEN_OPTS = $MAVEN_OPTS"); 

sub handle_errors {
	my $exit = shift;
	if ($exit == 0) {
		info("finished successfully");
	} elsif ($exit == -1) {
		error("failed to execute: $!");
	} elsif ($exit & 127) {
		error("child died with signal " . ($exit & 127));
	} else {
		error("child exited with value " . ($exit >> 8));
	}
	return $exit;
}

sub handle_errors_and_exit_on_failure {
	my $exit = handle_errors(@_);
	if ($exit != 0) {
		exit ($exit >> 8);
	}
}

sub handle_errors_and_exit {
	my $exit = handle_errors(@_);
	exit ($exit >> 8);
}

sub debug {
	print "[DEBUG] " . join(' ', @_) . "\n" if ($VERBOSE);
}

sub warning {
	print "[WARN] " . join(' ', @_) . "\n";
}

sub info {
	print "[INFO] " . join(' ', @_) . "\n";
}

sub error {
	print "[ERROR] " . join(' ', @_) . "\n";
}

1;
