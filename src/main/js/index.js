// @flow
import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import PushlogCommitter from "./PushlogCommitter";

binder.bind("changesets.author.suffix", PushlogCommitter);
