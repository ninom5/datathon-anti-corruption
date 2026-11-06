import {useMutation} from "@tanstack/react-query";
import {postFeedback} from "@/api";

/** Nothing public changes on submit (moderation comes first), so no query is invalidated. */
export const useSubmitFeedback = () => useMutation({mutationFn: postFeedback});
