#!/bin/sh
#
# Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
# This work is licensed under the Apache License, Version 2.0.
# You may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations.
#

#
# Start script for the Hocal AI UI.
# All application and dependency JARs live next to this script in "libs".
#
set -e

APP_HOME=$(cd -P "$(dirname "$0")" && pwd)

if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$JAVA_CMD" \
  --module-path "$APP_HOME/libs" \
  --add-modules ALL-MODULE-PATH \
  --enable-native-access=javafx.graphics \
  --module org.pcsoft.app.hocal.ui/org.pcsoft.app.hocal.app.LauncherKt \
  "$@"
