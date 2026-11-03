import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {BuyerPage, ComparisonPage, ContractPage, ContractsPage, HomePage, IndicatorPage, MapPage, NotFoundPage, SourcesPage} from "@/pages";
import {AppLayout} from "@/layouts";

export const Router = () => {
    return (
        <AppLayout>
            <Switch>
                <Route path={routes.HOME} component={HomePage}/>
                <Route path={routes.CONTRACTS} component={ContractsPage}/>
                <Route path={routes.CONTRACT} component={ContractPage}/>
                <Route path={routes.BUYER} component={BuyerPage}/>
                <Route path={routes.INDICATOR} component={IndicatorPage}/>
                <Route path={routes.SOURCES} component={SourcesPage}/>
                <Route path={routes.MAP} component={MapPage}/>
                <Route path={routes.COMPARISON} component={ComparisonPage}/>
                <Route component={NotFoundPage}/>
            </Switch>
        </AppLayout>
    );
};
