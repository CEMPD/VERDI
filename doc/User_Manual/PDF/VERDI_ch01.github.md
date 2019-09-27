\documentclass[12pt,]{}
\setcounter{section}{0}
\usepackage{lmodern}
\usepackage{amssymb,amsmath}
\usepackage{ifxetex,ifluatex}
\usepackage{fixltx2e} % provides \textsubscript
\ifnum 0\ifxetex 1\fi\ifluatex 1\fi=0 % if pdftex
  \usepackage[T1]{fontenc}
  \usepackage[utf8]{inputenc}
\else % if luatex or xelatex
  \ifxetex
    \usepackage{mathspec}
    \usepackage{xltxtra,xunicode}
  \else
    \usepackage{fontspec}
  \fi
  \defaultfontfeatures{Mapping=tex-text,Scale=MatchLowercase}
  \newcommand{\euro}{€}
    \setmainfont{Times New Roman}
    \setsansfont{Helvetica}
    \setmonofont[Mapping=tex-ansi]{Menlo}
\fi
% use upquote if available, for straight quotes in verbatim environments
\IfFileExists{upquote.sty}{\usepackage{upquote}}{}
% use microtype if available
\IfFileExists{microtype.sty}{%
\usepackage{microtype}
\UseMicrotypeSet[protrusion]{basicmath} % disable protrusion for tt fonts
}{}
\usepackage[margin=1in]{geometry}
\ifxetex
  \usepackage[setpagesize=false, % page size defined by xetex
              unicode=false, % unicode breaks when used with xetex
              xetex]{hyperref}
\else
  \usepackage[unicode=true]{hyperref}
\fi
\hypersetup{breaklinks=true,
            bookmarks=true,
            pdfauthor={},
            pdftitle={},
            colorlinks=true,
            citecolor=blue,
            urlcolor=blue,
            linkcolor=magenta,
            pdfborder={0 0 0}}
\urlstyle{same}  % don't use monospace font for urls
\usepackage{fancyhdr}
\pagestyle{fancy}
\pagenumbering{arabic}
\lhead{\itshape VERDI 2.0 User Manual}
\chead{}
\rhead{\itshape{\nouppercase{\leftmark}}}
\lfoot{v 2.0}
\cfoot{}
\rfoot{\thepage}
\setlength{\parindent}{0pt}
\setlength{\parskip}{6pt plus 2pt minus 1pt}
\setlength{\emergencystretch}{3em}  % prevent overfull lines
\providecommand{\tightlist}{%
  \setlength{\itemsep}{0pt}\setlength{\parskip}{0pt}}
\setcounter{secnumdepth}{0}

\title{VERDI 2.0 User Manual\\\vspace{0.5em}{\large 09/27/2019}}
\date{}

% Redefines (sub)paragraphs to behave more like sections
\ifx\paragraph\undefined\else
\let\oldparagraph\paragraph
\renewcommand{\paragraph}[1]{\oldparagraph{#1}\mbox{}}
\fi
\ifx\subparagraph\undefined\else
\let\oldsubparagraph\subparagraph
\renewcommand{\subparagraph}[1]{\oldsubparagraph{#1}\mbox{}}
\fi

\begin{document}
\maketitle

{
\hypersetup{linkcolor=black}
\setcounter{tocdepth}{}
\tableofcontents
\newpage
}
**Visualization Environment for Rich Data Interpretation (VERDI): User's
Manual**

U.S. EPA Contract No. EP-W-09-023, "Operation of the Center for
Community Air Quality Modeling and Analysis (CMAS)"<br> Prepared for:
Donna Schwede<br> U.S. EPA, ORD/NERL/AMD/APMB<br> E243-04 USEPA
Mailroom<br> Research Triangle Park, NC 27711<br> Prepared by: Liz
Adams<br> Institute for the Environment<br> The University of North
Carolina at Chapel Hill<br> 100 Europa Drive, Suite 490,CB 1105 <br>
Chapel Hill, NC 27599-1105<br> Date: September 26, 2019<br>

<a name="Introduction">

Introduction
============

</a>

Background
----------

This manual describes how to use the Visualization Environment for Rich
Data Interpretation (VERDI). VERDI is a flexible and modular Java-based
visualization software tool that allows users to visualize multivariate
gridded environmental datasets created by environmental modeling systems
such as the Community Multiscale Air Quality (CMAQ) modeling system, the
Weather Research and Forecasting (WRF) modeling system, and Model for
Prediction Across Scales (MPAS). These systems produce files of gridded
concentration and deposition fields that users need to visualize and
compare with observational data both spatially and temporally. VERDI can
facilitate these types of analyses.

Initial development of VERDI was done by the Argonne National Laboratory
for the U.S. Environmental Protection Agency (EPA) and its user
community. Argonne National Laboratory's work was supported by the EPA
through U.S. Department of Energy contract DE-AC02-06CH11357.  Further
development has been performed by the University of North Carolina
Institute for the Environment under U.S. EPA Contract No. EP-W-05-045
and EP-W-09-023, by Lockheed Corporation under U.S. EPA contract
No. 68-W-04-005, and Argonne National Laboratory.  VERDI is licensed
under the GNU General Public License (GPL) version 3, and the source
code is available through verdi.sourceforge.net.  Instructions for
developers within the community are included in the VERDI Developer
Instructions (see Section 1.3). VERDI is supported by the Community
Modeling and Analysis System (CMAS) Center under U.S. EPA Contract
No. EP-W-09-023. The batch script and VERDI Script Editor were developed
and documented under U.S. EPA Contract No. EP-D-07-102, through an
Office of Air Quality Planning and Standards project managed by Kirk
Baker. The CMAS Center is located within the Institute for the
Environment at the University of North Carolina at Chapel Hill.

This manual describes VERDI version 2.0 Beta released in July 2019.

The following are useful web links for obtaining VERDI downloads and
support:

1.  VERDI Visualization Tool web site:

<http://www.cmascenter.org/verdi>

1.  CMAS download page for users of VERDI (requires a CMAS account):

<https://www.cmascenter.org/download/forms/step_2.cfm?prod=11>

1.  CMAS GitHub website for developers of VERDI:

<https://github.com/CEMPD/VERDI>

1.  VERDI Frequently Asked Questions (FAQs):

<https://www.cmascenter.org/help/faq.cfm>

Use pulldown menu to select VERDI product to view its FAQs.

1.  To query and ask questions use the new CMAS User Forum, by selecting
    the VERDI category: <https://forum.cmascenter.org/c/verdi>

2.  To query the older M3USER listserv for VERDI related technical
    support questions and answers:
    <http://lists.unc.edu/read/?forum=m3user>

3.  To query bugs and submit bug reports, questions, and/or requests:

<https://github.com/CEMPD/VERDI/issues>

Where to Obtain VERDI
---------------------

You can download the latest version of VERDI from
<https://www.cmascenter.org/verdi/> (see [Figure1-1](#Figure1-1)
(fig. 1) and [Figure 1-2](#Figure1-2)). When you click on DOWNLOAD to
download VERDI, you will be sent to the CMAS Model Download Center. To
download and install VERDI, follow the instructions below, beginning at
step 4. Alternatively, you may also begin at the CMAS web site
<https://www.cmascenter.org>, and follow the instructions below:

1.  Log in using an existing CMAS account, or create a new CMAS account.

2.  Click the Download drop-down list and choose SOFTWARE.

3.  From the Software Download, Step 1 page go to the box Select
    Software to Download on the right side of the page. Use the
    drop-down list to select VERDI, and then click Submit.

4.  Select the product you wish to download, as shown in [Figure
    1-3](#Figure1-3). Also specify the type of computer on which you
    will run VERDI (i.e., Linux PC, Windows, or Other) from the items in
    the scroll list. Note that the compilers question is not relevant
    for VERDI so please select Not Applicable. Finally, click Submit.

5.  As shown in [Figure 1-4](#Figure1-4) follow the links to the
    appropriate version of the Linux, Mac, or Windows installation
    files. Links are also available for the current version of the
    documentation.

<a id=Figure1-1></a> Figure 1-1. Top of VERDI Page; note DOWNLOAD and
DOCUMENTATION links.<br>

![Figure 1: VERDI Download Page](./media/image001.png){#fig:Figure1-1}

<a id=Figure1-2></a> Figure 1-2. Bottom of VERDI Page<br>

![Figure1-2](./media/image002.png){Figure1-2}

<a id=Figure1-3></a> Figure 1-3. Downloading VERDI from the CMAS Web
Site, Step 2.<br>

![Figure1-3](./media/image003.png)

<a id=Figure1-4></a> Figure 1-4. Downloading VERDI from the CMAS Web
Site, Step 3<br>

![Figure1-4](./media/image004.png)

Where to Obtain VERDI Documentation
-----------------------------------

Documentation is available in several locations, described below. Each
location provides links to the available documentation for VERDI, which
can be viewed in your web browser or downloaded and saved to your
computer.

-   The main VERDI page (see [Figure 1-1](#Figure1-1)) has a link to
    Documentation.

-   The VERDI download page on the CMAS website (see [Figure
    1-4](#Figure1-4)) contains links to all of the available
    documentation.

-   On the left-hand side of the
    [www.cmascenter.org](http://www.cmascenter.org) web site, open the
    drop-down menu for Help and choose Documentation. Select the
    documentation for VERDI from the drop-down list ([Figure
    1-5](#Figure1-5)) and click Search. Select the model release from
    the drop-down list and click Search. The resulting documentation
    pane shows that the available documentation for the chosen release
    of VERDI (see [Figure 1-6](#Figure1-6)).

<a id=Figure1-5></a> Figure 1-5. VERDI Documentation on the CMAS Web
Site<br>

![Figure1-5](./media/image005.png)

-   To go directly to the most recent VERDI documentation click on
    DOCUMENTATION from the VERDI software:
    <http://www.cmascenter.org/verdi>. [Figure 1-6](#Figure1-6) shows
    the list of documentation that is available for download for VERDI
    2.0.

<a id=Figure1-6></a> Figure 1-6. VERDI Documentation on the CMAS Web
Site<br>

![Figure1-6](./media/image006.png)

Help Desk Support for VERDI
---------------------------

You are encouraged to search the new CMAS User Forum, by selecting the
VERDI category: <https://forum.cmascenter.org/c/verdi> or using the old
[M3USER
listserv](http://lists.unc.edu/read/search/results?forum=m3user&words=verdi&sb=1)
for VERDI-related technical support questions; report errors and/or
requests for enhancement to the m3user forum. The m3user forum is
supported by the community and also by CMAS to help users resolve issues
and identify and fix bugs found in supported software products.

Future VERDI Development
------------------------

As stated in Schwede et al. (2007),\[1\] "VERDI is intended to be a
community-based visualization tool with strong user involvement." The
VERDI source code is available to the public under a GNU Public License
(GPL) license at <https://github.com/CEMPD/VERDI>. This allows users who
wish to make improvements to VERDI to download the software, and to
develop enhancements and improvements that they believe may be useful to
the modeling community. Examples could include user-developed readers
for additional file formats and modules for additional plot types. Users
may wish to contribute data analysis routines, such as adding the
ability to do bilinear interpolation (smoothing), or to contribute other
enhancements to the existing plot types. The direction of future
development will depend on the resources and the needs of the modeling
community. If you are interested in contributing code to VERDI, please
review the information in Chapter 14, "Contributing to VERDI
Development."

\end{document}
