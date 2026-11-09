import {useMutation} from "@tanstack/react-query";
import {postFeedback} from "@/api";

export const useSubmitFeedback = () => useMutation({mutationFn: postFeedback});
