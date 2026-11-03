import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {ComparisonPage, ContractPage, ContractsPage, HomePage, MapPage, NotFoundPage, SourcesPage} from "@/pages";
import {AppLayout} from "@/layouts";

export const Router = () => {
    return (
        <AppLayout>
            <Switch>
                <Route path={routes.HOME} component={HomePage}/>
                <Route path={routes.CONTRACTS} component={ContractsPage}/>
                <Route path={routes.CONTRACT} component={ContractPage}/>
                <Route path={routes.SOURCES} component={SourcesPage}/>
                <Route path={routes.MAP} component={MapPage}/>
                <Route path={routes.COMPARISON} component={ComparisonPage}/>
                <Route component={NotFoundPage}/>
            </Switch>
        </AppLayout>
    );
};
