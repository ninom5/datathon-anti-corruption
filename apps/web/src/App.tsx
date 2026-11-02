import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {ErrorBoundary} from "react-error-boundary";
import {Router} from "@/Router.tsx";
import {ErrorPage} from "@/pages";
import {isClientError} from "@/api";

/** A 4xx (bad filter, unknown contract) will not change on retry; server and network errors get two more tries. */
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: (failureCount, error) => !isClientError(error) && failureCount < 2,
            refetchOnWindowFocus: false,
        },
    },
});

function App() {

    return (
        <ErrorBoundary FallbackComponent={ErrorPage}>
            <QueryClientProvider client={queryClient}>
                <Router/>
            </QueryClientProvider>
        </ErrorBoundary>
    )
}

export default App
